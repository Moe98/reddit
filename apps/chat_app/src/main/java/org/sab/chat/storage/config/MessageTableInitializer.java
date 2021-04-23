package org.sab.chat.storage.config;

import com.datastax.driver.core.Session;

public class MessageTableInitializer {

    public static void createMessageTable(Session session) {
        String query = "CREATE TABLE IF NOT EXISTS messages (" +
                "chat_id uuid, " +
                "message_id timeuuid, " +
                "content text, " +
                "sender_id uuid," +
                "PRIMARY KEY (chat_id, message_id)" +
                ") WITH CLUSTERING ORDER BY (message_id DESC);";
        session.execute(query);
    }
};
