package workers;

import beans.LiftRecord;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class WorkerManager implements AutoCloseable {
    private ArrayList<QueueWorker> workers;
    private Connection connection;
    private int MAX_WORKER = 10;
    private int CUR_WORKER = 1;
    private WorkerManager() throws IOException, TimeoutException {
        workers = new ArrayList<>();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("alapaka");
        factory.setPassword("123456");
        factory.setHost("35.89.12.60");//RabbitMQ ip address
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
    public boolean enqueue(LiftRecord record) throws IOException {
        boolean wait = workers.get(CUR_WORKER%MAX_WORKER).enQueue(record);
        CUR_WORKER++;
        return wait;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
