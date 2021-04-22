package org.sab.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServer {

    private static Connection connection;
    private Channel channel;
    private static RPCServer instance = null;

    private RPCServer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
    }

    public static RPCServer getInstance(String queueName) throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RPCServer();
        }

        instance.addChannel(queueName);
        return instance;
    }

    private void addChannel(String queueName) throws IOException, TimeoutException {
        try {
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);

            System.out.println(" [x] Awaiting RPC requests");

            Object monitor = new Object();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";

                try {
                    // TODO invoke command here
                    response += helloWorld();
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            channel.basicConsume(queueName, false, deliverCallback, (consumerTag -> { }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String helloWorld() {
        return"{\"msg\":\"Hello World\"}";
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        // TODO fix queue names.
        // TODO config file to initialize all queues + deal with wrong URIs (404 handle)
        RPCServer server = getInstance("/api_REQ");
    }

}
