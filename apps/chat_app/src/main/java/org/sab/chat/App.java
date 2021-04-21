package org.sab.chat;

import com.datastax.driver.core.Session;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.config.KeyspaceInitializer;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        CassandraConnector cassandra = new CassandraConnector();
        cassandra.connect("127.0.0.1", 9042);
        Session session = cassandra.getSession();
        System.out.println("Cassandra Connected");

        KeyspaceInitializer schemaRepository = new KeyspaceInitializer(session);
        String keyspaceName = "chat_app";
        schemaRepository.createKeyspace(keyspaceName, "SimpleStrategy", 1);

        cassandra.close();
        System.out.println("Cassandra Closed");
    }
}
