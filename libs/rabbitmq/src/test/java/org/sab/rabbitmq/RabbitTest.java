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

    final String message = "Test Test";
    final String expectedReplyMessage = "Test Reply";
    final String queueName = "TEST_QUEUE_REQ";
    final String replyQueueName = "TEST_QUEUE_RES";
    final int NUM_THREADS = 3;
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
            final SingleClientChannel channelExecutor = RPCClient.getSingleChannelExecutor();
            return channelExecutor.call(message, queueName, replyQueueName);
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

        String callbackReply = executorCallable1.get();
        assertEquals(callbackReply, expectedReplyMessage);

        String callbackSentMessage = this.receivedMessage;
        assertEquals(callbackSentMessage, message);
    }
}
