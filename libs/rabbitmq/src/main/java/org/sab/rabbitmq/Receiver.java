package org.sab.rabbitmq;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
//import java.util.logging.Level;

public class Receiver {
    private final static String QUEUE_NAME = "hello";
    ConnectionFactory factory;
    Connection connection;
    Channel channel;
    Consumer consumer;

    public Receiver() {
        try{
            this.factory = new ConnectionFactory();
            this.factory.setHost("localhost");
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for sad messages. To exit press CTRL+C");
            // Move the consumer here?
            this.consumer = new DefaultConsumer(this.channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                }
            };
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }


    public String receive() {
        if (consumer != null) {
            try {
                channel.basicConsume(QUEUE_NAME, true, consumer);
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
        r.receive();
    }
}
