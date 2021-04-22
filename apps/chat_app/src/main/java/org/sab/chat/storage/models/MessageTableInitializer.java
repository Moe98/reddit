package org.sab.chat.storage.models;

import com.datastax.driver.core.Session;

public class MessageTableInitializer {
    public static void createMessageTable(Session session) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append("messages").append("(")
                .append("chat_id uuid, ")
                .append("message_id timeuuid  PRIMARY KEY,")
                .append("content text, ")
                .append("sender_id uuid);");

        String query = sb.toString();
        session.execute(query);
    }
}
