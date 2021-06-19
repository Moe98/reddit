package org.sab.HttpServerUtilities;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClient {
    /**
     * Makes a getRequest to server deployed at localhost:8080
     *
     * @param uri assumes the uri is of the form api/appName (e.g api/user)
     */
    public static String get(String uri, String functionName) throws IOException, InterruptedException {
        return get(uri, functionName, 8080);
    }

    public static String get(String uri, String functionName, int port) throws IOException, InterruptedException {
        uri = String.format("http://localhost:%d/", port) + uri;
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri))
                .setHeader("Function-Name", functionName)
                .setHeader("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
