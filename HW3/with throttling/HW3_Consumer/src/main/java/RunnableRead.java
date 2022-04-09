import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RunnableRead implements Runnable {
    private static final int CHANGE_THRESHOLD = 100;
    private String channelName = null;
    private JedisPool pool;
    private Connection connection;
    private Channel channel;
    private String REQ_QUEUE_NAME;
    private int count;
    private LiftRecord curRecord;
    private boolean forSkiers;
    private Jedis jedis;
    private int currentActive;
    private DeliverCallback deliverCallback = (tag, delivery) -> {
        try {
            String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);

            //SharedHashMap.put(count, msg);
            if (jedis == null) jedis = pool.getResource();
            String[] fields = msg.split(" ");
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].equals("LiftRecord")) {
                    String time = fields[i + 1];
                    String liftId = fields[i + 2];
                    String waitTime = fields[i + 3];
                    int resortId = Integer.parseInt(fields[i + 4]);
                    int skiersId = Integer.parseInt(fields[i + 5]);
                    int seasonId = Integer.parseInt(fields[i + 6]);
                    int daysId = Integer.parseInt(fields[i + 7]);
                    curRecord = new LiftRecord(time, liftId, waitTime, resortId, skiersId, seasonId, daysId);
                    break;
                }
            }
            if (curRecord != null) {
                Transaction transaction = jedis.multi();
                String key = channelName + "_Record:" + count;
                transaction.hmset(key, curRecord.toHashMap());
                if (forSkiers) transaction.zadd("SkierSearch", curRecord.getSkiersId(), key);
                else transaction.zadd("ResortSearch", curRecord.getResortId(), key);
                transaction.exec();
                count++;
                //jedis.save();
                currentActive--;
                //if(count%CHANGE_THRESHOLD==0)jedis.save();//save each 10 changes
            }
            //jedis.set(channelName + count,msg);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            //channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, resp.getBytes(StandardCharsets.UTF_8));
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    };

    public RunnableRead(JedisPool pool, Connection connection, String channelName, boolean forSkier) throws IOException, TimeoutException {
        this.pool = pool;
        this.connection = connection;
        this.channelName = channelName;
        this.forSkiers = forSkier;
        this.currentActive = 0;
        count = 0;
        REQ_QUEUE_NAME = forSkier ? "For_skiers" : "For_resorts";
        if (this.connection != null) {
            channel = this.connection.createChannel();
            channel.basicQos(1);
        }
        if (channel != null) {
            channel.queueDeclare(REQ_QUEUE_NAME, false, false, false, null);
        }
    }

    @Override
    public void run() {
        try {

            channel.basicConsume(REQ_QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public String getREQ_QUEUE_NAME() {
        return REQ_QUEUE_NAME;
    }

    public void setREQ_QUEUE_NAME(String REQ_QUEUE_NAME) {
        this.REQ_QUEUE_NAME = REQ_QUEUE_NAME;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public DeliverCallback getDeliverCallback() {
        return deliverCallback;
    }

    public void setDeliverCallback(DeliverCallback deliverCallback) {
        this.deliverCallback = deliverCallback;
    }
}
