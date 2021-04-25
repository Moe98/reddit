package org.sab.chat.storage.tables;

import org.sab.chat.storage.config.CassandraConnector;

public class DirectChatTable {

    public static final String TABLE_NAME = "direct_chats";

    private CassandraConnector cassandra;

    public DirectChatTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "chat_id uuid, " +
                "first_member uuid, " +
                "second_member uuid, " +
                "PRIMARY KEY (chat_id));";
        cassandra.runQuery(query);
    }
}
