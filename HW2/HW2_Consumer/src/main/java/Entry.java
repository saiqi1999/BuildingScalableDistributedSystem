import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class Entry implements AutoCloseable{
    private static Connection connection;
    public static int MAX_THREAD = 20;
    public static void main(String[] args) throws IOException, TimeoutException {
        ArrayList<Thread> threads = new ArrayList<>();
        ConcurrentHashMap<Integer,String> concurrentHashMap = new ConcurrentHashMap<>();

        for(int i = 0; i < MAX_THREAD; i++){
            Thread t = new Thread(new RunnableRead(concurrentHashMap));
            threads.add(t);
        }
        for(Thread t : threads) t.start();
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
