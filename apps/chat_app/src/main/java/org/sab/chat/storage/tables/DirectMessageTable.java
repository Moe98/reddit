package org.sab.chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;

import org.sab.chat.storage.models.DirectChat;
import org.sab.chat.storage.models.DirectMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DirectMessageTable {

    public static final String TABLE_NAME = "direct_messages";

    private CassandraConnector cassandra;


    private Mapper<DirectMessage> mapper;

    public DirectMessageTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;
    }

    public void createMapper(){
        MappingManager manager = new MappingManager(cassandra.getSession());
        this.mapper = manager.mapper(DirectMessage.class);
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
        createMapper();
    }

    public UUID createDirectMessage(UUID chat_id, UUID sender_id, String content) throws InvalidInputException {

        UUID message_id =  UUIDs.timeBased();

        try {
            UUID.fromString(sender_id.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid user UUID.");
        }

        try {
            UUID.fromString(chat_id.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid chat UUID.");
        }

        String query0 = "SELECT * FROM " + "direct_chats" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";
        ResultSet queryResult0 = cassandra.runQuery(query0);
        List<Row> query0Rows = queryResult0.all();
        if (query0Rows == null || query0Rows.size() == 0)
            throw new InvalidInputException("There is no chat between users");

        String query1 = "SELECT * FROM " + "direct_chats" +
                " WHERE chat_id = " + chat_id + "AND first_member = " + sender_id + " ALLOW FILTERING;";
        String query2 = "SELECT * FROM " + "direct_chats" +
                " WHERE chat_id = " + chat_id + "AND second_member = " + sender_id + " ALLOW FILTERING;";

        ResultSet queryResult1 = cassandra.runQuery(query1);
        ResultSet queryResult2 = cassandra.runQuery(query2);


        List<Row> query1Rows = queryResult1.all();
        List<Row> query2Rows = queryResult2.all();

        if (((query1Rows == null || query1Rows.size() == 0) && (query2Rows == null || query2Rows.size() == 0)))
            throw new InvalidInputException("Not a chat member");
        mapper.save(new DirectMessage(chat_id, message_id, sender_id, content));

        return message_id;
    }

    public List<String> getDirectMessages(UUID chat_id, UUID user) throws InvalidInputException {
        try {
            UUID.fromString(user.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid user UUID.");
        }

        try {
            UUID.fromString(chat_id.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid chat UUID.");
        }
        String query0 = "SELECT * FROM " + "direct_chats" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";
        ResultSet queryResult0 = cassandra.runQuery(query0);
        List<Row> query0Rows = queryResult0.all();
        if (((query0Rows == null || query0Rows.size() == 0))) {
            throw new InvalidInputException("Chat does not exist");
        }
        UUID first_member = query0Rows.get(0).get(1, UUID.class);
        UUID second_member = query0Rows.get(0).get(2, UUID.class);
        if (!first_member.toString().equals(user.toString()) && !second_member.toString().equals(user.toString()))
            throw new InvalidInputException("Not a chat member");

        String query1 = "SELECT content FROM " + "direct_messages" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";

        ResultSet queryResult1 = cassandra.runQuery(query1);
        List<Row> query1Rows = queryResult1.all();

        List<String> messages = new ArrayList<>();
        for (int i = 0; i < query1Rows.size(); i++) {
            messages.add(query1Rows.get(i).get(0, String.class));
        }

        return messages;

    }

    public Mapper<DirectMessage> getMapper() {
        return mapper;
    }

};
