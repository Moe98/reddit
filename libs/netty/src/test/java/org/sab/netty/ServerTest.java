package org.sab.netty;

import com.rabbitmq.client.*;
import org.junit.Test;
import org.sab.HttpServerUtilities.HttpClient;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class ServerTest {
    static String response = "";
    int NUM_THREADS = 5;
    String queueName = "TEST_APP_REQ";
    String receivedMessage = "";
    String expectedReplyMessage = "{\"msg\":\"Not Found\"}";

    /**
     * Much Rigorous Test :-)
     * Clean Code++
     * No need to trust me that it works:(
     * ┏(・o・)┛
     */


    @Test
    public void serverReturnsNotFound() {
        final int numThreads = NUM_THREADS;
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

        final Runnable startServer = () -> {
            try {
                Server.main(null);
            } catch (CertificateException | SSLException | InterruptedException e) {
                e.printStackTrace();
            }
        };

        final Runnable shutdownServer = () -> {
            Server.shutdownGracefully();
        };


        final Runnable runRabbitClient = () -> {
            try {
                response = HttpClient.get("api/test_app", "HELLO_WORLD");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };

        final Callable<String> respondToClient = () -> {
            ConnectionFactory factory = new ConnectionFactory();
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            factory.setHost("localhost");
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

            return channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        };

        final Future<?> declareQueuesFuture = threadPool.submit(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(queueName, false, false, false, null).getQueue();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });

        while (!declareQueuesFuture.isDone()) ;

        threadPool.submit(startServer);

        final Future<?> sendRequestFuture = threadPool.submit(runRabbitClient);
        final Future<String> sendResponseFuture = threadPool.submit(respondToClient);

        while (!sendRequestFuture.isDone() || !sendResponseFuture.isDone()) ;

        final Future<?> shutdownServerFuture = threadPool.submit(shutdownServer);

        while (!shutdownServerFuture.isDone()) ;

        assertEquals(expectedReplyMessage, response);
    }

}
