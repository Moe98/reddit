package org.sab.chat;

import org.sab.chat.storage.config.CassandraConnector;

public class App {
    public static void main(String[] args) {
        CassandraConnector cassandra = new CassandraConnector();
        cassandra.connect();
        System.out.println("Cassandra Connected");
        cassandra.close();
        System.out.println("Cassandra Closed");
    }
}
