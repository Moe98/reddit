package org.sab.rabbitmq;

import com.rabbitmq.client.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Sender {
    ConnectionFactory factory;
    Connection connection;
    Channel channel;

    public Sender() {
        this.factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
//            this.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void send(JSONObject message, String queueName) {
        try {
            // TODO check if queue exists first
            this.channel.queueDeclare(queueName, false, false, false, null);
            channel.basicPublish("", queueName, null, message.toString().getBytes("UTF-8"));

//            AMQP.BasicProperties props = new BasicProperties
//                    .Builder()
//                    .replyTo(callbackQueueName)
//                    .correlationId()
//                    .build();

            System.out.println(" [x] Sent '" + message + "'");
            this.channel.close();
            this.connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] argv)  {
        Sender sender = new Sender();
        final String QUEUE_NAME = "hello";
        JSONObject message = new JSONObject();
        message.put("message", "Hello Moe, Manta, Lujine!;");
        sender.send(message, QUEUE_NAME);

        sender = new Sender();
        message = new JSONObject();
        message.put("message", "GoodbyeWorld, no yeet");
        sender.send(message, QUEUE_NAME);

    }

}
