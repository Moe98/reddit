package org.sab.chat.storage.tables;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupChat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupChatTable {

    public static final String TABLE_NAME = "group_chats";

    private CassandraConnector cassandra;
    private Mapper<GroupChat> mapper;

    public GroupChatTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;

        MappingManager manager = new MappingManager(cassandra.getSession());
        this.mapper = manager.mapper(GroupChat.class);
    }

    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "chat_id uuid, " +
                "name text, " +
                "description text, " +
                "members list<uuid>, " +
                "admin uuid, " +
                "date_created timestamp, " +
                "PRIMARY KEY (chat_id));";
        cassandra.runQuery(query);
    }

    public void createGroupChat(UUID creator, String name, String description) throws InvalidInputException {
        UUID chatId = UUID.randomUUID();

        if(name == null || name.length() == 0)
            throw new InvalidInputException("Group name cannot be empty or null.");
        if(description == null)
            throw new InvalidInputException("Group name cannot be null.");

        try {
           UUID.fromString(creator.toString());
        } catch (IllegalArgumentException e){
            throw new InvalidInputException("Invalid Admin UUID.");
        }

        List<UUID> membersList = new ArrayList<>();
        membersList.add(creator);
        mapper.save(new GroupChat(chatId, name, description, membersList, creator));
    }


}
