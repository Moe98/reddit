package org.sab.chat.storage.tables;

import org.sab.chat.storage.config.CassandraConnector;

public class ChatTable {

    public static final String TABLE_NAME = "chats";

    private CassandraConnector cassandra;

    public ChatTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "chat_id uuid, " +
                "name text, " +
                "description text, " +
                "members list<uuid>, " +
                "admin uuid, " +
                "date_created timestamp, " +
                "PRIMARY KEY (chat_id));";
        cassandra.runQuery(query);
    }
}
