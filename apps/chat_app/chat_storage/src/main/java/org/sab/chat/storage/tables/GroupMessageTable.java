package org.sab.chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;

import org.sab.chat.storage.models.GroupMessage;

import java.util.List;
import java.util.UUID;

public class GroupMessageTable {

    public static final String TABLE_NAME = "group_messages";

    private final CassandraConnector cassandra;


    private Mapper<GroupMessage> mapper;

    public GroupMessageTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;
    }

    public void createMapper(){
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
        createMapper();
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


        String query1 = "SELECT members FROM " + "group_chats" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";

        ResultSet queryResult = cassandra.runQuery(query1);
        List<Row> query1Rows = queryResult.all();
        if (TableUtils.isEmpty(query1Rows)) {
            throw new InvalidInputException("Invalid chat id");
        }

        List<UUID> ls = query1Rows.get(0).getList(0, UUID.class);
        if (!ls.contains(sender_id))
            throw new InvalidInputException("Not a chat member");

        mapper.save(new GroupMessage(chat_id, message_id, sender_id, content));

        return message_id;

    }

    public List<GroupMessage> getGroupMessages(UUID chat_id, UUID user) throws InvalidInputException {
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
        List<Row> query0Rows = queryResult0.all();
        if (TableUtils.isEmpty(query0Rows)) {
            throw new InvalidInputException("Chat does not exist");
        }
        List<UUID> members = query0Rows.get(0).getList(4, UUID.class);
        if (!members.contains(user))
            throw new InvalidInputException("Not a chat member");

        String query1 = "SELECT * FROM " + "group_messages" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";

        ResultSet messages = cassandra.runQuery(query1);
        return mapper.map(messages).all();
    }

    public Mapper<GroupMessage> getMapper() {
        return mapper;
    }
}
