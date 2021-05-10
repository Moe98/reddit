package org.sab.chat.storage.tables;


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectChat;

import java.util.UUID;

public class DirectChatTable {

    public static final String TABLE_NAME = "direct_chats";

    private final CassandraConnector cassandra;
    private Mapper<DirectChat> mapper;

    public DirectChatTable(CassandraConnector cassandra) {

        this.cassandra = cassandra;
    }

    public void createMapper(){
        MappingManager manager = new MappingManager(cassandra.getSession());
        this.mapper = manager.mapper(DirectChat.class);
    }

    public void createTable() {

        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "chat_id uuid, " +
                "first_member uuid, " +
                "second_member uuid, " +
                "PRIMARY KEY (chat_id));";
        cassandra.runQuery(query);
        createMapper();
    }

    public UUID createDirectChat(UUID first_member, UUID second_member) throws InvalidInputException {
        UUID chatId = UUID.randomUUID();
        try {
            UUID.fromString(first_member.toString());
            UUID.fromString(second_member.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid user UUID.");
        }
        String query1 = "SELECT * FROM " + TABLE_NAME +
                " WHERE first_member = " + first_member + "AND second_member = " + second_member + " ALLOW FILTERING;";
        String query2 = "SELECT * FROM " + TABLE_NAME +
                " WHERE first_member = " + second_member + "AND second_member = " + first_member + " ALLOW FILTERING;";

        ResultSet queryResult1 = cassandra.runQuery(query1);
        ResultSet queryResult2 = cassandra.runQuery(query2);

        if (!TableUtils.isEmpty(queryResult1.all()) || !TableUtils.isEmpty(queryResult2.all()))
            throw new InvalidInputException("Chat already exist between Users");
        mapper.save(new DirectChat(chatId, first_member, second_member));
        return chatId;
    }

    public Mapper<DirectChat> getMapper() {
        return mapper;
    }

}