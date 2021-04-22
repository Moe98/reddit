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

    private RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
    }

    public static RPCClient getInstance() throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RPCClient();
        }

        instance.addChannel();
        return instance;
    }

    private void addChannel() throws IOException, TimeoutException {
        channel = connection.createChannel();
    }

//    public static void main(String[] argv) {
//        try (RPCClient fibonacciRpc = getInstance()) {
//            for (int i = 0; i < 32; i++) {
//                String i_str = Integer.toString(i);
//                System.out.println(" [x] Requesting fib(" + i_str + ")");
//                String response = fibonacciRpc.call(i_str, "SendQueue", "ReceiveQueue");
//                System.out.println(" [.] Got '" + response + "'");
//            }
//        } catch (IOException | TimeoutException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    public String call(String message, String reqQueueName, String replyQueueName) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();

        String resQueueName = channel.queueDeclare(reqQueueName, false, false, false, null).getQueue();
        String replyQueue = channel.queueDeclare(replyQueueName, false, false, false, null).getQueue();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueue)
                .build();

        channel.basicPublish("", resQueueName, props, message.getBytes("UTF-8"));

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channel.basicConsume(replyQueueName, /*isAutoAck=*/ true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.offer(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {});

        String result = response.take();
        channel.basicCancel(ctag);
        return result;
    }

    public void close() throws IOException {
        connection.close();
    }
}
