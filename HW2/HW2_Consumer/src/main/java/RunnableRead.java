import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class RunnableRead implements Runnable {
    private ConcurrentHashMap<Integer, String> SharedHashMap;
    private Connection connection;
    private Channel channel;
    private String REQ_QUEUE_NAME;
    private int count;
    private DeliverCallback deliverCallback = (tag, delivery) -> {
        /*AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(delivery.getProperties().getCorrelationId())
                .build();
        String resp = "";*/
        try {
            String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
            //String[] frags = msg.split(" ");
            //LiftRecord record = new LiftRecord(frags[0], frags[1], frags[2]);
            SharedHashMap.put(count, msg);
            //System.out.println("[.]put in hash map: id=" + frags[1] + " data=" + record);
            //resp += delivery.getProperties().getCorrelationId();//ack to take this piece.
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            //channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, resp.getBytes(StandardCharsets.UTF_8));
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    };

    public RunnableRead(ConcurrentHashMap<Integer, String> sharedHashMap) throws IOException, TimeoutException {
        SharedHashMap = sharedHashMap;
        count = 0;
        REQ_QUEUE_NAME = "From_Worker";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("alapaka");
        factory.setPassword("123456");
        factory.setHost("34.214.54.205");//TODO changeable
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(REQ_QUEUE_NAME, false, false, false, null);
    }

    @Override
    public void run() {
        while (true) {
            try {
                channel.basicConsume(REQ_QUEUE_NAME, false, deliverCallback, consumerTag -> {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
