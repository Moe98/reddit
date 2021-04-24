package org.sab.netty;

import com.rabbitmq.client.*;
import org.junit.Test;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class ServerTest {
    int NUM_THREADS = 3;
    String queueName = "TEST_QUEUE";
    String receivedMessage = "";
    String expectedReplyMessage = "{\"msg\":\"Hello World\"}";
    static String response = "";

    /**
     * Much Rigorous Test :-)
     * Clean Code++
     * No need to trust me that it works:(
     * ┏(・o・)┛
     */
    public String get(String uri, String functionName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Function-Name", functionName)
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    @Test
    public void serverWorking() {
        int threads = NUM_THREADS;
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);

        Runnable runRabbitClient = () -> {
            try {
                Server.main(null);
                response = get("http://localhost:8080/api/example_app", "HELLO_WORLD");
            } catch (CertificateException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };

        Callable<String> respondToClient = () -> {
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


        Future<?> executorCallable1 = threadPool.submit(runRabbitClient);
        Future<String> executorCallable2 = threadPool.submit(respondToClient);

        while (!executorCallable1.isDone() || !executorCallable2.isDone()) ;
        System.out.println("Done with callables");

        assertEquals(response, expectedReplyMessage);

    }
}