import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class Entry implements AutoCloseable {
    private static Connection connection;
    public static int MAX_THREAD = 100;

    public static void main(String[] args) throws IOException, TimeoutException {

        //changeable redis ip
        String ip = "35.89.32.219";
        //changeable RMQ ip
        String MQip = "18.237.50.255";
        //if For Skier, index data by skier id
        //otherwise, index data by resort id
        boolean ForSkier = true;
        for(int i = 0; i < args.length; i++){
            if(args[i].equals("-ip"))ip = args[i+1];
            if(args[i].equals("-m"))ForSkier = args[i+1].equals("ForSkier");
            if(args[i].equals("-MQip"))MQip = args[i+1];
        }

        JedisPool pool = new JedisPool(ip, 6379);
        pool.setMaxTotal(MAX_THREAD);
        //test for redis connection
        /*Jedis jedis = pool.getResource();
        //Set<String> set = jedis.keys("*");
        //for(String s : set) jedis.del(s);
        Transaction transaction = jedis.multi();
        for (int i = 0; i < 10; i++) {

            LiftRecord record = new LiftRecord("time:" + i, i + "", "wait:" + i);
            transaction.hmset("LiftRecord:" + i, record.toHashMap());
            transaction.zadd("recordId", i, "LiftRecord:" + i);

            //jedis.set(String.valueOf(i), String.valueOf(record));
        }
        transaction.exec();
        jedis.save();
        System.out.println(jedis.keys("LiftRecord*"));
        System.out.println("specific id query: " + jedis.zrangeByScore("recordId", 7, 7));
        System.out.println("id range search:" + jedis.zrangeByScore("recordId", 0, 7));
        //System.out.println("query all for one record" + jedis.hgetAll("LiftRecord:*")); no wildcard
        System.out.println("query all for one record" + jedis.hgetAll("LiftRecord:3"));*/


        ArrayList<Thread> threads = new ArrayList<>();

        //connection only 1
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("alapaka");
        factory.setPassword("123456");
        factory.setHost(MQip);//changeable rabbitmq ip
        connection = factory.newConnection();
        //JedisPool pool = new JedisPool("localhost",6379);

        for(int i = 0; i < MAX_THREAD; i++){
            Thread t = new Thread(new RunnableRead(pool,connection,"channel"+i, ForSkier));
            threads.add(t);
        }
        for(Thread t : threads) t.start();
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
