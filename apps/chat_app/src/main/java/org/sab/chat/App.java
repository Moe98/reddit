package org.sab.chat;

import com.datastax.driver.core.Session;
import org.sab.chat.storage.config.CassandraConnector;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        CassandraConnector cassandra = new CassandraConnector();
        cassandra.connect("127.0.0.1", 9042);
        Session session = cassandra.getSession();
        System.out.println("Cassandra Connected");

        cassandra.close();
        System.out.println("Cassandra Closed");
    }
}
