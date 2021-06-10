package org.sab.chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectChat;
import org.sab.chat.storage.models.GroupChat;

import java.sql.Timestamp;
import java.util.*;

public class GroupChatTable {

    public static final String TABLE_NAME = "group_chats";

    private final CassandraConnector cassandra;

    private Mapper<GroupChat> mapper;

    public GroupChatTable(CassandraConnector cassandra) {
        this.cassandra = cassandra;
    }

    public void createMapper() {
        MappingManager manager = new MappingManager(cassandra.getSession());
        this.mapper = manager.mapper(GroupChat.class);
    }

    public void createTable() {
        String tableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "chat_id uuid, " +
                "name text, " +
                "description text, " +
                "members list<uuid>, " +
                "admin uuid, " +
                "date_created timestamp, " +
                "PRIMARY KEY (chat_id));";
        cassandra.runQuery(tableQuery);

        String indexQuery = "CREATE INDEX IF NOT EXISTS ON " + TABLE_NAME + " (members);";
        cassandra.runQuery(indexQuery);

        createMapper();
    }

    public GroupChat createGroupChat(UUID creator, String name, String description) throws InvalidInputException {
        UUID chatId = UUID.randomUUID();

        if (name == null || name.length() == 0)
            throw new InvalidInputException("Group name cannot be empty or null.");
        if (description == null)
            throw new InvalidInputException("Description cannot be null.");

        try {
            UUID.fromString(creator.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid Admin UUID.");
        }

        List<UUID> membersList = new ArrayList<>();
        membersList.add(creator);
        Date date = new Date();
        GroupChat createdGroupChat = new GroupChat(chatId, name, description, membersList, creator, new Timestamp(date.getTime()));
        mapper.save(createdGroupChat);
        return createdGroupChat;
    }
    public GroupChat getGroupChat(UUID chatId) throws InvalidInputException {
        try {
            UUID.fromString(chatId.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid chat UUID.");
        }

        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE chat_id = " + chatId + " ALLOW FILTERING;";

        ResultSet groupChats = cassandra.runQuery(query);
        return mapper.map(groupChats).all().get(0);
    }

    public List<GroupChat> getGroupChats(UUID userId) throws InvalidInputException {
        try {
            UUID.fromString(userId.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid user UUID.");
        }

        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE members CONTAINS " + userId + " ALLOW FILTERING;";

        ResultSet groupChats = cassandra.runQuery(query);
        return mapper.map(groupChats).all();
    }

    public GroupChat addGroupMember(UUID chatId, UUID adminId, UUID memberId) throws InvalidInputException {
        try {
            UUID.fromString(chatId.toString());
            UUID.fromString(adminId.toString());
            UUID.fromString(memberId.toString());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid UUID.");
        }
        String query1 = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chatId + " ALLOW FILTERING;";

        ResultSet queryResult = cassandra.runQuery(query1);

        if (TableUtils.isEmpty(queryResult.all())) {
            throw new InvalidInputException("Chat does not exist");
        }

        String query2 = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chatId + " AND admin = " + adminId + " ALLOW FILTERING;";
        ResultSet query2Result = cassandra.runQuery(query2);
        List<Row> query2Rows = query2Result.all();
        if (TableUtils.isEmpty(query2Rows)) {
            throw new InvalidInputException("Not the admin to add members");
        }

        String name = query2Rows.get(0).get(5, String.class);
        String description = query2Rows.get(0).get(3, String.class);
        List<UUID> members = query2Rows.get(0).getList(4, UUID.class);
        Date dateCreated = query2Rows.get(0).getTimestamp(2);

        if (members.contains(memberId)) {
            throw new InvalidInputException("Member already there");
        }
        members.add(memberId);

        GroupChat updatedGroupChat = new GroupChat(chatId, name, description, members, adminId, dateCreated);

        mapper.save(updatedGroupChat);

        return updatedGroupChat;
    }

    public GroupChat removeGroupMember(UUID chatId, UUID adminId, UUID memberId) throws InvalidInputException {
        try {
            UUID.fromString(chatId.toString());
            UUID.fromString(adminId.toString());
            UUID.fromString(memberId.toString());

        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid UUID.");
        }
        String query = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chatId + " ALLOW FILTERING;";

        ResultSet queryResult = cassandra.runQuery(query);
        List<Row> query1Rows = queryResult.all();

        if (TableUtils.isEmpty(query1Rows)) {
            throw new InvalidInputException("Chat does not exist");
        }

        String query1 = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chatId + " AND admin = " + adminId + " ALLOW FILTERING;";
        ResultSet query1Result = cassandra.runQuery(query1);
        List<Row> query2Rows = query1Result.all();
        if (TableUtils.isEmpty(query2Rows)) {
            throw new InvalidInputException("Not the admin to add members");
        }

        String name = query2Rows.get(0).get(5, String.class);
        String description = query2Rows.get(0).get(3, String.class);
        List<UUID> members = query2Rows.get(0).getList(4, UUID.class);
        Date dateCreated = query2Rows.get(0).getTimestamp(2);

        if (!members.contains(memberId)) {
            throw new InvalidInputException("Member not in group");
        }
        members.remove(memberId);
        GroupChat updatedGroupChat = new GroupChat(chatId, name, description, members, adminId, dateCreated);
        mapper.save(updatedGroupChat);

        return updatedGroupChat;
    }

    public GroupChat leavesChat(UUID chatId, UUID userId) throws InvalidInputException {
        try {
            UUID.fromString(chatId.toString());
            UUID.fromString(userId.toString());

        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid UUID.");
        }

        String query = "SELECT * FROM " + "group_chats" +
                " WHERE chat_id = " + chatId + " ALLOW FILTERING;";

        ResultSet queryResult = cassandra.runQuery(query);
        List<Row> all = queryResult.all();

        if (((all == null || all.size() == 0)))
            throw new InvalidInputException("Chat does not exist");


        String name = all.get(0).get(5, String.class);
        List<UUID> members = all.get(0).getList(4, UUID.class);
        String description = all.get(0).get(3, String.class);
        Date date_created = all.get(0).getTimestamp(2);
        UUID admin = all.get(0).get(1, UUID.class);

        if (!members.contains(userId) && !admin.equals(userId)) {
            throw new InvalidInputException("Member not in group");
        }
        GroupChat updatedGroupChat = mapper.get(chatId);
        if (members.contains(userId) && admin.equals(userId)) {
            mapper.delete(chatId);
        } else if (members.contains(userId)) {
            members.remove(userId);
            updatedGroupChat = new GroupChat(chatId, name, description, members, admin, date_created);
            mapper.save(updatedGroupChat);
        }

        return updatedGroupChat;
    }

    public Mapper<GroupChat> getMapper() {
        return mapper;
    }
}

