import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

public class ClearAll {
    public static void main(String[] args) {
        String ip = "54.245.79.84";
        JedisPool pool = new JedisPool(ip, 6379);
        Jedis jedis = pool.getResource();
        Set<String> set = jedis.keys("*");
        for(String s : set) jedis.del(s);
        jedis.save();
    }
}
