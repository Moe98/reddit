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

        instance.addChannel();
        instance.addQueue(queueName);
        return instance;
    }

    // adding a channel to the connection, along with it's listener
    private void addChannel() throws IOException, TimeoutException {
        try {
            // creating a channel
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Channel getChannel() {
        return channel;
    }

    private void addQueue(String queueName) throws IOException {
        // initializing the queue which the RCPServer is constantly listening to
        channel.queueDeclare(queueName, false, false, false, null);
    }

    private void x(String queueName) throws IOException, TimeoutException {
        try {
            // creating a channel
            channel = connection.createChannel();

            // initializing the queue which the RCPServer is constantly listening to
            channel.queueDeclare(queueName, false, false, false, null);

//            System.out.println(" [x] Awaiting RPC requests");


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
