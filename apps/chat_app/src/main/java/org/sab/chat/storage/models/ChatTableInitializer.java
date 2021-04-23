package org.sab.chat.storage.models;

import com.datastax.driver.core.Session;

public class ChatTableInitializer {

    public static void createChatTable(Session session) {
        String query = "CREATE TABLE IF NOT EXISTS chats (" +
                "chat_id uuid, " +
                "name text, " +
                "description text, " +
                "members list<uuid>, " +
                "admin uuid, " +
                "date_created timestamp, " +
                "PRIMARY KEY (chat_id));";
        session.execute(query);
    }
}
