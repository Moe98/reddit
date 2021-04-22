package org.sab.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RPCClient implements AutoCloseable {

    private static Connection connection;
    private Channel channel;
    private static RPCClient instance = null;

    // creating a connection with RabbitMQ
    private RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
    }

    // creating a singleton of the RPCClient
    public static RPCClient getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RPCClient();
        }

        instance.addChannel();
        return instance;
    }

    // adding a channel to the connection
    private void addChannel() throws IOException, TimeoutException {
        channel = connection.createChannel();
    }

    // sending the message to the request queue and receiving the response from the respond queue
    public String call(String message, String reqQueueName, String replyQueueName) throws IOException, InterruptedException {
        // creating a unique identifier for each request put in the request queue
        final String corrId = UUID.randomUUID().toString();

        // initializing the request queue
        String resQueueName = channel.queueDeclare(reqQueueName, false, false, false, null).getQueue();
        // initializing the response queue
        String replyQueue = channel.queueDeclare(replyQueueName, false, false, false, null).getQueue();

        // adding the corrID of the request
        // adding the name of the response queue that the client server will listen for a response on
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueue)
                .build();

        // sending the request message in the request queue with it's properties
        channel.basicPublish("", resQueueName, props, message.getBytes("UTF-8"));

        // creating a blocking queue to put the expected response in
        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        // listening on the expected response queue
        // with a callback that checks that the response queue received a response message with corrID equal to request corrID
        // and putting the response message in the blocking queue response
        String ctag = channel.basicConsume(replyQueueName, /*isAutoAck=*/ true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {});

        // attempting to retrieve the response from the blocked queue
        // if response not yet present, block the RPCClient until the callback fn. above is able to retrieve the message
        String result = response.take();

        // cancelling any further consumptions from the channel
        channel.basicCancel(ctag);

        // returning the response message
        return result;
    }

    // closing the connection with RabbitMQ
    public void close() throws IOException {
        connection.close();
    }
}
