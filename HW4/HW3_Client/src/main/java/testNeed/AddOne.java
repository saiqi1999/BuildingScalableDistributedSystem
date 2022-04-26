package testNeed;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

public class AddOne {
    public static void main(String[] args) {
        String[] skierIps = {"54.213.162.5"};
        String[] resortIps = {"54.69.36.225"};
        addOne(skierIps,resortIps);
    }

    private static void addOne(String[] ips, String[] resortIps) {
        LiftRecord curRecord = new LiftRecord("time","3","wTime",1,1,1,1);
        for(String ip : ips){
            JedisPool pool = new JedisPool(ip, 6379);
            Jedis jedis = pool.getResource();
            Transaction transaction = jedis.multi();
            String key = "testChannelName" + "_Record:" + 1;//special unique key for this record
            transaction.hmset(key, curRecord.toHashMap());
            transaction.zadd("SkierSearch", curRecord.getSkiersId(), key);//basic index get all of this ID
            transaction.zadd(curRecord.getResortId()
                    +"_"+curRecord.getSeasonId()
                    +"_"+curRecord.getDaysId(), curRecord.getSkiersId(), key);//index given resort,season,day
            transaction.zadd(curRecord.getResortId()
                    +"_"+curRecord.getSeasonId(), curRecord.getSkiersId(), key);//index given resort,season
            transaction.zadd(String.valueOf(curRecord.getResortId()),curRecord.getSkiersId(),key);//index given resort
            transaction.exec();
        }
        for(String ip : resortIps){
            JedisPool pool = new JedisPool(ip, 6379);
            Jedis jedis = pool.getResource();
            Transaction transaction = jedis.multi();
            String key = "testChannelName" + "_Record:" + 1;//special unique key for this record
            transaction.hmset(key, curRecord.toHashMap());
            transaction.zadd("ResortSearch", curRecord.getResortId(), key);//basic index to resort
            transaction.zadd(curRecord.getSeasonId()+"_"+curRecord.getDaysId()
                    , curRecord.getResortId(), key);//index given days and season to locate a resort
            transaction.exec();
        }
    }
}
