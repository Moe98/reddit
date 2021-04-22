package org.sab.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServer {

    private static Connection connection;
    private Channel channel;
    private static RPCServer instance = null;

    // creating a connection with RabbitMQ
    private RPCServer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
    }

    // creating a singleton of the RPCServer
    public static RPCServer getInstance(String queueName) throws IOException, TimeoutException {
        if (instance == null) {
            instance = new RPCServer();
        }

        instance.addChannel(queueName);
        return instance;
    }

    // adding a channel to the connection, along with it's listener
    private void addChannel(String queueName) throws IOException, TimeoutException {
        try {
            // creating a channel
            channel = connection.createChannel();

            // initializing the queue which the RCPServer is constantly listening to
            channel.queueDeclare(queueName, false, false, false, null);

            System.out.println(" [x] Awaiting RPC requests");

            Object monitor = new Object();

            // creating a callback which would invoke the required command specified by the JSON request message
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                // adding the corrID of the response, which is the corrID of the request
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";

                try {
                    // TODO invoke command here
                    // invoking command
                    response += helloWorld();
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {
                    // sending response message to the response queue, which is defined by the request message's properties
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));

                    // sending a manual acknowledgement of sending the response
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            // listening to the request queue, ready to consume a request and process it using the above callback
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
