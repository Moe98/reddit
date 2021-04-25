package org.sab.chat.storage.tables;

import org.sab.chat.storage.config.CassandraConnector;

public class DirectMessageTable {

    public static final String TABLE_NAME = "direct_messages";

    private CassandraConnector cassandra;

    public DirectMessageTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "chat_id uuid, " +
                "message_id timeuuid, " +
                "content text, " +
                "sender_id uuid," +
                "PRIMARY KEY (chat_id, message_id)" +
                ") WITH CLUSTERING ORDER BY (message_id DESC);";
        cassandra.runQuery(query);
    }
};
