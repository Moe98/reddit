package org.sab.chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectMessage;
import org.sab.chat.storage.models.GroupMessage;

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
        for (UUID id : ls) {
            if (id.toString().equals(sender_id)) {
                found = true;
            }
        }
        if (!found)
            throw new InvalidInputException("Not a chat member");

        mapper.save(new GroupMessage(chat_id, message_id, sender_id, content));

        return message_id;

    }

    public Mapper<GroupMessage> getMapper() {
        return mapper;
    }
}
