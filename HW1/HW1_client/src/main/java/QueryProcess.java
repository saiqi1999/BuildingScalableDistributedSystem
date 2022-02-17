import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class QueryProcess implements Runnable {
    private static final int CREATED = 201;
    private CountDownLatch latch;
    private int scheduledTimes;
    private String destIp;
    private int idStart;
    private int idEnd;
    private int phaseStart;
    private int phaseEnd;
    private Random random;
    private int successCount;
    private int failCount;

    public QueryProcess(CountDownLatch latch, String ip, int idStart, int idEnd, int phaseStart, int phaseEnd, int scheduledTimes) {
        this.latch = latch;
        this.destIp = ip;
        this.idStart = idStart;
        this.idEnd = idEnd;
        this.phaseStart = phaseStart;
        this.phaseEnd = phaseEnd;
        this.scheduledTimes = scheduledTimes;
        random = new Random();
        successCount = 0;
        failCount = 0;
    }

    @Override
    public void run() {
        HttpClient client = new HttpClient();

        for (int i = 0; i < scheduledTimes; i++) {
            String uri = "http://"+destIp+"/HW1_try2/skiers/";// change ip
            //PostMethod method = new PostMethod("http://localhost:8080/HW1_server_war_exploded/skiers/*/seasons/*/days/*/skiers/*");
            //insert parameters
            int idRange = idEnd - idStart;
            int curId = Math.abs(random.nextInt()) % idRange + idStart; //uri pos 4
            int resortId = Math.abs(random.nextInt());//uri pos 1
            int seasonsId = Math.abs(random.nextInt());//uri pos 2
            int day = Math.abs(random.nextInt()) % 366;//uri pos 3

            int waitTime = Math.abs(random.nextInt()) % 11;//request param 3
            int liftId = Math.abs(random.nextInt());//request para 2
            int phaseRange = phaseEnd - phaseStart;
            int curTime = Math.abs(random.nextInt()) % phaseRange + phaseStart;//request para 1

            uri = uri + resortId + "/seasons/" + seasonsId + "/days/" + day + "/skiers/" + curId;
            PostMethod postMethod = new PostMethod(uri);
            postMethod.addParameter("time", String.valueOf(curTime));
            postMethod.addParameter("liftId", String.valueOf(liftId));
            postMethod.addParameter("waitTime", String.valueOf(waitTime));

            //retry 5 times if failed using method
            HttpMethodRetryHandler handler = new DefaultHttpMethodRetryHandler(5,false);
            HttpMethodParams params = new HttpMethodParams();
            //params.setParameter("time", String.valueOf(curTime));
            params.setParameter(HttpMethodParams.RETRY_HANDLER, handler);
            postMethod.setParams(params);

            try {
                client.executeMethod(postMethod);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println(postMethod.getStatusCode());
            if(postMethod.getStatusCode() == CREATED)successCount++;
            else failCount++;
            postMethod.releaseConnection();

        }
        Starter.increment(successCount,failCount);
        latch.countDown();
    }
}
