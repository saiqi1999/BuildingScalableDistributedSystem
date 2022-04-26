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
    /**
     * here, we'll decide how we store the data in Redis
     * changed in HW4:
     * we need to have 3 ways of getting info, therefore, 3 ways indexes are added:
     * 1, we have skier id only, that means search for all record
     * 2, get ski day vertical for a skier, we have resortID, seasonID, dayID, skierID
     * => an index of resortID_seasonID_dayID, key of skierID to get all record
     * 3, we have resortID, seasonID, skierID
     * => an index of resortID_seasonID, key of skierID
     *
     * we also check unique skiers given resort, season, day. so build extra index use season and day to locate resort
     * count unique skiers manually, this should not be called frequently, so it's tradeoff,
     * we have to make sure we can hand over our reason - all record that queried
     */
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
                String key = channelName + "_Record:" + count;//special unique key for this record
                transaction.hmset(key, curRecord.toHashMap());
                if (forSkiers) {
                    transaction.zadd("SkierSearch", curRecord.getSkiersId(), key);//basic index get all of this ID
                    transaction.zadd(curRecord.getResortId()
                            +"_"+curRecord.getSeasonId()
                            +"_"+curRecord.getDaysId(), curRecord.getSkiersId(), key);//index given resort,season,day
                    transaction.zadd(curRecord.getResortId()
                            +"_"+curRecord.getSeasonId(), curRecord.getSkiersId(), key);//index given resort,season
                    transaction.zadd(String.valueOf(curRecord.getResortId()),curRecord.getSkiersId(),key);//index given resort
                }
                else {
                    transaction.zadd("ResortSearch", curRecord.getResortId(), key);//basic index to resort
                    transaction.zadd(curRecord.getSeasonId()+"_"+curRecord.getDaysId()
                            , curRecord.getResortId(), key);//index given days and season to locate a resort
                }
                transaction.exec();
                count++;
            }
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
