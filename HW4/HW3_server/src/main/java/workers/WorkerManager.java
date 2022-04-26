package workers;

import beans.LiftRecord;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class WorkerManager implements AutoCloseable {
    private ArrayList<QueueWorker> workers;
    private Connection connection;
    private static final String RMQip = "34.213.38.180";
    private int MAX_WORKER = 10;
    private int CUR_WORKER = 1;
    private Jedis jedis = null;
    private Jedis resortJedis = null;
    public void setSkierIP(String skierDbIP) {
        JedisPool pool = new JedisPool(skierDbIP, 6379);
        jedis = pool.getResource();
    }
    public void setResortIP(String resortDbIP) {
        try{
            JedisPool pool = new JedisPool(resortDbIP, 6379);
            resortJedis = pool.getResource();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private WorkerManager() throws IOException, TimeoutException {
        workers = new ArrayList<>();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("alapaka");
        factory.setPassword("123456");
        factory.setHost(RMQip);//RabbitMQ ip address
        connection = factory.newConnection();
        for(int i = 0; i <MAX_WORKER; i++){
            workers.add(new QueueWorker(connection, i));
        }
    };
    private static WorkerManager manager;
    public static WorkerManager getManager() throws IOException, TimeoutException {
        if(manager == null){
            synchronized (WorkerManager.class){
                if (manager == null){
                    manager = new WorkerManager();
                }
            }
        }
        return manager;
    }
    public void enqueue(LiftRecord record) throws IOException {
        workers.get(CUR_WORKER%MAX_WORKER).enQueue(record);
        CUR_WORKER++;
    }

    /**
     * query sending to SKIER redis server
     */
    public Set<LiftRecord> query(String paraResortID, String paraSeasonID, String dayID, int skierID) {

        if(jedis == null)return null;

        if(dayID!=null&& !dayID.equals("") &&paraResortID!=null&&paraSeasonID!=null&& !paraSeasonID.equals("") && !paraResortID.equals("")){
            //use resort_season_day index
            List<String> keys = jedis.zrangeByScore(paraResortID+"_"+paraSeasonID+"_"+dayID, skierID,skierID);
            return getLiftRecordsSet(keys);
        }
        if(paraResortID!=null&&paraSeasonID!=null&& !paraSeasonID.equals("") && !paraResortID.equals("")){
            //use resort_season index
            List<String> keys = jedis.zrangeByScore(paraResortID+"_"+paraSeasonID, skierID,skierID);
            return getLiftRecordsSet(keys);
        }
        if(paraResortID!=null&& !paraResortID.equals("")){
            List<String> keys = jedis.zrangeByScore(paraResortID, skierID,skierID);
            return getLiftRecordsSet(keys);
        }
        return null;
    }

    /**
     * query sending to RESORTS redis server
     */
    public Set<LiftRecord> uniqueIn(int resortID, int seasonID, int dayID) {
        //use season_day index, but setting ip
        List<String> keys = resortJedis.zrangeByScore(seasonID+"_"+dayID, resortID, resortID);
        return getLiftRecordsSet(keys);
    }

    private Set<LiftRecord> getLiftRecordsSet(List<String> keys) {
        Set<LiftRecord> ans = new HashSet<>();
        for(String key : keys){
            Map<String, String> prop = jedis.hgetAll(key);
            LiftRecord record = new LiftRecord(prop);
            ans.add(record);
        }
        return ans;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }



}
