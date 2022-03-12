package workers;


import beans.LiftRecord;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class QueueWorker {
    private Connection connection;
    private Channel channel;
    private String ReqQueueName;
    private String ReplyQueueName;
    private int workerId;
    public QueueWorker(Connection connection, int workerId) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //connectionFactory.setUsername();
        this.workerId = workerId;
        this.connection = connection;
        this.channel = connection.createChannel();
        long createTime = System.currentTimeMillis();
        ReqQueueName = "From_Worker";
        ReplyQueueName = "To_Worker_"+workerId;
        channel.queueDeclare(ReqQueueName,false,false,false,null);
    }
    public void enQueue(LiftRecord record) throws IOException {
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties properties = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(ReplyQueueName)
                .build();
        channel.basicPublish("",ReqQueueName,properties,record.toString().getBytes(StandardCharsets.UTF_8));

    }

    public void close() throws IOException {
        connection.close();
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

    public String getReqQueueName() {
        return ReqQueueName;
    }

    public void setReqQueueName(String reqQueueName) {
        ReqQueueName = reqQueueName;
    }

    public String getReplyQueueName() {
        return ReplyQueueName;
    }

    public void setReplyQueueName(String replyQueueName) {
        ReplyQueueName = replyQueueName;
    }
}
