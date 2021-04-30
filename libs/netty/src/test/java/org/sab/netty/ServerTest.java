package org.sab.netty;

import com.rabbitmq.client.*;
import org.junit.Test;
import org.sab.demo.ExampleApp;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.concurrent.*;

import javax.net.ssl.SSLException;

import static org.junit.Assert.*;

public class ServerTest {
    int NUM_THREADS = 5;
    String queueName = "TEST_APP_REQ";
    String receivedMessage = "";
    String expectedReplyMessage = "{\"msg\":\"Not Found\"}";
    static String response = "";

    /**
     * Much Rigorous Test :-)
     * Clean Code++
     * No need to trust me that it works:(
     * ┏(・o・)┛
     */
    public String get(String uri, String functionName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).header("Function-Name", functionName)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

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
                response = get("http://localhost:8080/api/test_app", "HELLO_WORLD");
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

        while (!declareQueuesFuture.isDone());

        threadPool.submit(startServer);

        final Future<?> sendRequestFuture = threadPool.submit(runRabbitClient);
        final Future<String> sendResponseFuture = threadPool.submit(respondToClient);

        while (!sendRequestFuture.isDone() || !sendResponseFuture.isDone());

        final Future<?> shutdownServerFuture = threadPool.submit(shutdownServer);

        while (!shutdownServerFuture.isDone());

        System.out.println("Done with callables");
        assertEquals(expectedReplyMessage, response);
    }

    public void runServer(){
        new Thread(() -> {
            try {
                Server.main(null);
            } catch (Exception e) {
                fail("Server did not start\n" +e.getMessage());
            }
        }).start();
    }
    public void runExampleApp(){
        new Thread(() -> {
            try {
                ExampleApp.main(null);
            } catch (Exception e) {
                fail("Example App did not start\n" +e.getMessage());
            }
        }).start();
    }

    @Test
    public void callingExampleAppAPI() {
        runServer();
        runExampleApp();
        String response= null;
        try {
            response = get("http://localhost:8080/api/example_app","HELLO_WORLD");
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        assertEquals(response, "{\"msg\":\"Hello World\"}");

    }
}
