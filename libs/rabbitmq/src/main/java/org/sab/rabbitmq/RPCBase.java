package org.sab.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;
import java.util.Map;

import java.nio.charset.StandardCharsets;


public abstract class RPCBase {

    private static final String LOCALHOST  = "localhost";

    protected final Connection connection;
    protected Channel channel;

    protected RPCBase() throws TimeoutException, IOException {
        this.connection = initConnection();
    }

    private static Connection initConnection() throws TimeoutException, IOException {
        ConnectionFactory factory = new ConnectionFactory();

        String rabbitHost = System.getenv("RABBIT_HOST");
        if(rabbitHost == null)
            rabbitHost = LOCALHOST;

        factory.setHost(rabbitHost);

        return factory.newConnection();
    }

    // adding a channel to the connection
    protected void addChannel() throws IOException {
        channel = connection.createChannel();
    }

    protected String declareQueue(String queue,
                                  boolean durable,
                                  boolean exclusive,
                                  boolean autoDelete,
                                  Map<String, Object> arguments) throws IOException {

        final String declaredQueue = channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments).getQueue();

        return declaredQueue;
    }

    private BasicProperties createSenderProps(String corrId, String replyToQueue) {

        // adding the corrID of the request
        // adding the name of the response queue that the client server will listen for
        // a response on
        return new BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyToQueue)
                .build();
    }

    protected void sendRequest(String corrId, String message, String targetQueue, String replyToQueue)
            throws IOException {

        final BasicProperties senderProps = createSenderProps(corrId, replyToQueue);
        final String exchange = "";
        // sending the request message in the request queue with it's properties
        channel.basicPublish(exchange, targetQueue, senderProps, message.getBytes(StandardCharsets.UTF_8));
    }


    protected void sendRequest_withoutReplyTo(String corrId, String message, String targetQueue)
            throws IOException {

        final BasicProperties senderProps = createSenderProps(corrId, "");
        final String exchange = "";
        // sending the request message in the request queue with it's properties
        channel.basicPublish(exchange, targetQueue, senderProps, message.getBytes(StandardCharsets.UTF_8));
    }
}
