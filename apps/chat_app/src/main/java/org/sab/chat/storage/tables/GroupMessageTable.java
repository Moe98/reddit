package org.sab.chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;

import org.sab.chat.storage.models.GroupMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupMessageTable {

    public static final String TABLE_NAME = "group_messages";

    private CassandraConnector cassandra;


    private Mapper<GroupMessage> mapper;

    public GroupMessageTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;

        MappingManager manager = new MappingManager(cassandra.getSession());
        this.mapper = manager.mapper(GroupMessage.class);
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

    public UUID createGroupMessage(UUID chat_id, UUID sender_id, String content) throws InvalidInputException {

        UUID message_id = UUIDs.timeBased();

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


        String query = "SELECT members FROM " + "group_chats" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";

        ResultSet queryResult = cassandra.runQuery(query);
        List<Row> all = queryResult.all();
        if (((all == null || all.size() == 0))) {
            throw new InvalidInputException("Invalid chat id");
        }

        boolean found = false;
        List<UUID> ls = all.get(0).getList(0, UUID.class);
        if (!ls.contains(sender_id))
            throw new InvalidInputException("Not a chat member");

        mapper.save(new GroupMessage(chat_id, message_id, sender_id, content));

        return message_id;

    }

    public List<String> getGroupMessages(UUID chat_id, UUID user) throws InvalidInputException {
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
        String query0 = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";
        ResultSet queryResult0 = cassandra.runQuery(query0);
        List<Row> all0 = queryResult0.all();
        if (((all0 == null || all0.size() == 0))) {
            throw new InvalidInputException("Chat does not exist");
        }
        List<UUID> members = all0.get(0).getList(4, UUID.class);
        if (!members.contains(user))
            throw new InvalidInputException("Not a chat member");

        String query1 = "SELECT content FROM " + "group_messages" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";

        ResultSet queryResult1 = cassandra.runQuery(query1);
        List<Row> all1 = queryResult1.all();

        List<String> messages = new ArrayList<>();
        for (int i = 0; i < all1.size(); i++) {
            messages.add(all1.get(i).get(0, String.class));
        }

        return messages;

    }

    public Mapper<GroupMessage> getMapper() {
        return mapper;
    }
}
