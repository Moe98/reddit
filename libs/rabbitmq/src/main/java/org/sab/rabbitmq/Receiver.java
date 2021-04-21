package org.sab.rabbitmq;

import com.rabbitmq.client.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
//import java.util.logging.Level;

public class Receiver {
    ConnectionFactory factory;
    Connection connection;
    Channel channel;
    Consumer consumer;

    String message;

    public Receiver() {
        try{
            this.factory = new ConnectionFactory();
            this.factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            System.out.println(" [*] Waiting for sad messages. To exit press CTRL+C");
            // Move the consumer here?
//                AMQP.BasicProperties props = new AMQP.BasicProperties
//                        .Builder()
//                        .correlationId(this.correlationId)
//                        .replyTo(queueName)
//                        .build();
            this.consumer = new DefaultConsumer(this.channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    Receiver.this.message = message;
                    System.out.println(" [x] Received '" + message + "'");
                }
            };
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }


    public JSONObject receive(String queueName) {
        if (consumer != null) {
            try {
                // TODO check if queue exists
                channel.queueDeclare(queueName, false, false, false, null);
                channel.basicConsume(queueName, true, consumer);
                JSONObject jsonObject = new JSONObject(message);
                return jsonObject;
            } catch (IOException e) {
                e.printStackTrace();
            }
//            return consumer.receive();
        }
//        else {
//
//            return consumer.receive();
//                // TODO create consumer.
////                conn = config.connect();
////                session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
////
////                Destination destination = session.createQueue(config.getQueueName());
////                consumer = session.createConsumer(destination);
//
//        }
        return null;
    }

    public static void main(String[] argv) {
        // TODO figure out where Receiver is called.
        Receiver r = new Receiver();
        final String QUEUE_NAME = "hello";

        //TODO how to consume only 1 message?
        System.out.println(r.receive(QUEUE_NAME));
    }
}
