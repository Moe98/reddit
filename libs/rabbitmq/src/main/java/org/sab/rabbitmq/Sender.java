package org.sab.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Sender {
    private final static String QUEUE_NAME = "hello";

    ConnectionFactory factory;
    Connection connection;
    Channel channel;

    public Sender() {
        this.factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            this.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {

        try {
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
            this.channel.close();
            this.connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv)  {
        Sender s = new Sender();
        String message = "Hello Moe, Manta, Lujine!";
        s.send(message);

        s = new Sender();
        message = "o";
        s.send(message);

    }

}
