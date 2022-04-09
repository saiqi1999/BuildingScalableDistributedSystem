package workers;


import beans.LiftRecord;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class QueueWorker {
    private final String ReqQueueName1;
    private final String ReqQueueName2;
    private Connection connection;
    private Channel channel;
    private String ReplyQueueName;
    private boolean circuitBreak;
    private int holdTime;
    private static final int CircuitBreakThreshold = 1000;//circuit break when how many in queue

    public QueueWorker(Connection connection, int workerId) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //connectionFactory.setUsername();
        this.connection = connection;
        this.channel = connection.createChannel();
        long createTime = System.currentTimeMillis();
        ReqQueueName1 = "For_skiers";
        ReqQueueName2 = "For_resorts";
        ReplyQueueName = "To_Worker_"+workerId;
        channel.queueDeclare(ReqQueueName1,false,false,false,null);
        channel.queueDeclare(ReqQueueName2,false,false,false,null);
        holdTime = 0;
        circuitBreak = true;//apply throttling?
    }
    public boolean enQueue(LiftRecord record) throws IOException {
        final String corrId = UUID.randomUUID().toString();
        AMQP.BasicProperties properties = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(ReplyQueueName)
                .build();
        //passive query, for circuit breaker
        AMQP.Queue.DeclareOk sta = channel.queueDeclare(ReqQueueName1,false,false,false,null);
        boolean ans = sta.getMessageCount()>CircuitBreakThreshold;
        //System.out.println(sta.getMessageCount());
        if(sta.getMessageCount()>CircuitBreakThreshold){
            holdTime = 1000;
        }
        try {
            if(circuitBreak)Thread.sleep(holdTime);
            holdTime/=2;//exponentially increase if hold time don't reset
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channel.basicPublish("",ReqQueueName1,properties,record.toString().getBytes(StandardCharsets.UTF_8));
        channel.basicPublish("",ReqQueueName2,properties,record.toString().getBytes(StandardCharsets.UTF_8));
        return ans;
    }

    public void close() throws IOException {
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getReplyQueueName() {
        return ReplyQueueName;
    }

    public void setReplyQueueName(String replyQueueName) {
        ReplyQueueName = replyQueueName;
    }

    public String getReqQueueName1() {
        return ReqQueueName1;
    }

    public String getReqQueueName2() {
        return ReqQueueName2;
    }

    public boolean isCircuitBreak() {
        return circuitBreak;
    }

    public void setCircuitBreak(boolean circuitBreak) {
        this.circuitBreak = circuitBreak;
    }

    public int getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(int holdTime) {
        this.holdTime = holdTime;
    }
}
