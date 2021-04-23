package org.sab.netty;

import org.junit.Test;
import org.sab.rabbitmq.RPCServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class ServerTest {

    public String get(String uri) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public void runServer() {
        // creating a thread for running the netty HTTP server
        new Thread(() -> {
            try {
                Server.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // creating a thread for running the RPCServer "example mini app"
//        new Thread(() -> {
//            try {
//                RPCServer.getInstance("/api_REQ");
//            } catch(Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    @Test
    public void serverWorking() throws IOException, InterruptedException, TimeoutException {
        runServer();

        // TODO fix test
//        String response = get("http://localhost:8080/api");
        // TODO this will need to be more generic in the future.
//        assertEquals(response, "{\"msg\":\"Hello World\"}");


    }
}