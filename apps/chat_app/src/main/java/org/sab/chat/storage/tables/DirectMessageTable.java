package org.sab.chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectChat;
import org.sab.chat.storage.models.DirectMessage;
import org.sab.chat.storage.models.GroupChat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DirectMessageTable {

    public static final String TABLE_NAME = "direct_messages";

    private CassandraConnector cassandra;


    private Mapper<DirectMessage> mapper;

    public DirectMessageTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;

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
    }

    public UUID createDirectMessage(UUID chat_id, UUID sender_id, String content) throws InvalidInputException {

        UUID message_id =  UUIDs.timeBased();

        try {
            UUID.fromString(sender_id.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid user UUID.");
        }

        try {
            UUID.fromString(message_id.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid message UUID.");
        }



        String query1 = "SELECT * FROM " + "direct_chats" +
                " WHERE chat_id = " + chat_id + "AND first_member = " + sender_id + " ALLOW FILTERING;";
        String query2 = "SELECT * FROM " + "direct_chats" +
                " WHERE chat_id = " + chat_id + "AND second_member = " + sender_id + " ALLOW FILTERING;";

        ResultSet queryResult1 = cassandra.runQuery(query1);
        ResultSet queryResult2 = cassandra.runQuery(query2);


        List<Row> all1 = queryResult1.all();
        List<Row> all2 = queryResult2.all();

        if (((all1 == null || all1.size() == 0) && (all2 == null || all2.size() == 0)))
            throw new InvalidInputException("There is no chat between users");
        mapper.save(new DirectMessage(chat_id, message_id, sender_id, content));

        return message_id;
    }

    public Mapper<DirectMessage> getMapper() {
        return mapper;
    }

};
