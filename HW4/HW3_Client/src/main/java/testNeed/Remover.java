package testNeed;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.Set;

public class Remover {
    public static void main(String[] args) {
        String[] ips = {"54.213.162.5","54.69.36.225"};
        removeAll(ips);
    }
    //removing all data in Redis, for test & will not publish
    private static void removeAll(String[] ips) {
        for(String ip : ips){
            JedisPool pool = new JedisPool(ip, 6379);
            Jedis jedis = pool.getResource();
            jedis.flushAll();
        }
    }
}
