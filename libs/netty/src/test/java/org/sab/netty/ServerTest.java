package org.sab.netty;

import org.junit.Test;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Assertions;
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
        new Thread(() -> {
            try {
                Server.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Test
    public void serverWorking() throws IOException, InterruptedException {
        runServer();
        String response = get("http://localhost:8080/api");
        // TODO this will need to be more generic in the future.
        assertEquals(response, "{\"msg\":\"Hello World\"}");
    }
    public void runDB() {
        try {
            PostgresConnection pg = PostgresConnection.getInstance();
            Connection c = pg.connect();
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT 1");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
            assertFalse(rs.next());
        } catch (PropertiesNotLoadedException | SQLException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void postgresWorking() {
        runDB();
        assertTrue(true);
    }

    @Test
    public void postgresIsSingleton() throws PropertiesNotLoadedException {
        PostgresConnection conn1 = PostgresConnection.getInstance();
        PostgresConnection conn2 = PostgresConnection.getInstance();
        assertTrue(conn1 == conn2);
    }

    @Test
    public void canCloseConnection() throws PropertiesNotLoadedException {
        PostgresConnection postgresConnection = PostgresConnection.getInstance();
        Connection conn = postgresConnection.connect();
        postgresConnection.closeConnection(conn);
    }

    @Test
    public void cantUseClosedConnection() throws PropertiesNotLoadedException {
        PostgresConnection postgresConnection = PostgresConnection.getInstance();
        Connection conn = postgresConnection.connect();
        postgresConnection.closeConnection(conn);

        Assertions.assertThrows(org.postgresql.util.PSQLException.class, () -> {
            conn.createStatement();
        });
    }
}