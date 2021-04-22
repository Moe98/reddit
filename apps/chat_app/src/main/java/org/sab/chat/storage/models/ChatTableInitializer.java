package org.sab.chat.storage.models;

import com.datastax.driver.core.Session;

public class ChatTableInitializer {

    public static void createChatTable(Session session) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append("chats").append("(")
                .append("chat_id uuid PRIMARY KEY, ")
                .append("name text,")
                .append("description text,")
                .append("members list<uuid>,")
                .append("admin uuid, ")
                .append("date_created timestamp);");

        String query = sb.toString();
        session.execute(query);
    }
}
