package org.sab.chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupChat;

import java.sql.Timestamp;
import java.util.*;

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

    public UUID createGroupChat(UUID creator, String name, String description) throws InvalidInputException {
        UUID chatId = UUID.randomUUID();

        if (name == null || name.length() == 0)
            throw new InvalidInputException("Group name cannot be empty or null.");
        if (description == null)
            throw new InvalidInputException("Group name cannot be null.");

        try {
            UUID.fromString(creator.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid Admin UUID.");
        }

        List<UUID> membersList = new ArrayList<>();
        membersList.add(creator);
        Date date = new Date();
        mapper.save(new GroupChat(chatId, name, description, membersList, creator, new Timestamp(date.getTime())));
        return chatId;
    }

    public UUID addGroupMember(UUID chat_id, UUID admin, UUID user) throws InvalidInputException {
        try {
            UUID.fromString(chat_id.toString());
            UUID.fromString(admin.toString());
            UUID.fromString(user.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid UUID.");
        }
        String query = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";

        ResultSet queryResult = cassandra.runQuery(query);
        List<Row> all = queryResult.all();

        if (((all == null || all.size() == 0))) {
            throw new InvalidInputException("Chat does not exist");
        }

        String query1 = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chat_id + " AND admin = " + admin + " ALLOW FILTERING;";
        ResultSet query1Result = cassandra.runQuery(query1);
        List<Row> all1 = query1Result.all();
        if (((all1 == null || all1.size() == 0))) {
            throw new InvalidInputException("Not the admin to add members");
        }

        String name = all1.get(0).get(5, String.class);
        String description = all1.get(0).get(3, String.class);
        List<UUID> members = all1.get(0).getList(4, UUID.class);
        Date date_created = all1.get(0).getTimestamp(2);

        members.add(user);

        mapper.save(new GroupChat(chat_id, name, description, members, admin, date_created));

        return user;
    }

    public UUID removeGroupMember(UUID chat_id, UUID admin, UUID user) throws InvalidInputException {
        try {
            UUID.fromString(chat_id.toString());
            UUID.fromString(admin.toString());
            UUID.fromString(user.toString());

        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid UUID.");
        }
        String query = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";

        ResultSet queryResult = cassandra.runQuery(query);
        List<Row> all = queryResult.all();

        if (((all == null || all.size() == 0))) {
            throw new InvalidInputException("Chat does not exist");
        }

        String query1 = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chat_id + " AND admin = " + admin + " ALLOW FILTERING;";
        ResultSet query1Result = cassandra.runQuery(query1);
        List<Row> all1 = query1Result.all();
        if (((all1 == null || all1.size() == 0))) {
            throw new InvalidInputException("Not the admin to add members");
        }

        String name = all1.get(0).get(5, String.class);
        String description = all1.get(0).get(3, String.class);
        List<UUID> members = all1.get(0).getList(4, UUID.class);
        Date date_created = all1.get(0).getTimestamp(2);


        if (!members.contains(user)) {
            throw new InvalidInputException("Member not in group");
        }
        members.remove(user);
        mapper.save(new GroupChat(chat_id, name, description, members, admin, date_created));

        return user;
    }

    public UUID leavesChat(UUID chat_id, UUID user) throws InvalidInputException {
        try {
            UUID.fromString(chat_id.toString());
            UUID.fromString(user.toString());

        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid UUID.");
        }

        String query = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chat_id + " ALLOW FILTERING;";

        ResultSet queryResult = cassandra.runQuery(query);
        List<Row> all = queryResult.all();

        if (((all == null || all.size() == 0)))
            throw new InvalidInputException("Chat does not exist");


        String name = all.get(0).get(5, String.class);
        List<UUID> members = all.get(0).getList(4, UUID.class);
        String description = all.get(0).get(3, String.class);
        Date date_created = all.get(0).getTimestamp(2);
        UUID admin = all.get(0).get(1, UUID.class);

        if (!members.contains(user) && !admin.toString().equals(user.toString())) {
            throw new InvalidInputException("Member not in group");
        }
        if (members.contains(user) && admin.toString().equals(user.toString())) {
            mapper.delete(chat_id);
        } else if (members.contains(user)) {
            members.remove(user);
            mapper.save(new GroupChat(chat_id, name, description, members, admin, date_created));
        }

        return user;
    }

    public Mapper<GroupChat> getMapper() {
        return mapper;
    }
}

