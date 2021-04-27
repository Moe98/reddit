package org.sab.rabbitmq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.rabbitmq.client.*;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * Unit test for Rabbit App.
 * I guess the turtle was faster after all...
 */
public class RabbitTest {
    //    static String receivedMessage;
    String message = "Test Test";
    String expectedReplyMessage = "Test Reply";
    String queueName = "TEST_QUEUE";
    String replyQueueName = "TEST_QUEUE_REPLY";
    int NUM_THREADS = 3;
    String receivedMessage = "";

    /**
     * Much Rigorous Test :-)
     * Very Complex Test :-)
     * Wow :o
     */
    @Test
    public void testRabbit() throws ExecutionException, InterruptedException {
        int threads = NUM_THREADS;
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);

        Callable<String> runRabbitClient = () -> {
            RPCClient client = RPCClient.getInstance();
            return client.call(message, queueName, replyQueueName);
        };

        Callable<String> respondToClient = () -> {
            ConnectionFactory factory = new ConnectionFactory();
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            factory.setHost("localhost");
            channel.queueDeclare(queueName, false, false, false, null).getQueue();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    receivedMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);
                } finally {

                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(delivery.getProperties().getCorrelationId())
                            .build();
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, expectedReplyMessage.getBytes(StandardCharsets.UTF_8));

                }

            };

            return channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        };


        Future<String> executorCallable1 = threadPool.submit(runRabbitClient);
        Future<String> executorCallable2 = threadPool.submit(respondToClient);


        while (!executorCallable1.isDone() || !executorCallable2.isDone()) ;
        System.out.println("Done with callables");

        String callbackReply = executorCallable1.get();
        System.out.println("Callback Reply: " + callbackReply);
        assertEquals(callbackReply, expectedReplyMessage);

        String callbackSentMessage = this.receivedMessage;
        System.out.println("Callback 2 Reply: " + callbackSentMessage);
        assertEquals(callbackSentMessage, message);
    }
}
