package org.sab.chat.storage.tables;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupMessage;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;


public class GroupMessageTableTest {

    private CassandraConnector cassandra;
    private GroupMessageTable groupMessages;

    @Before
    public void connect() {
        cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();

        groupMessages = new GroupMessageTable(cassandra);
        groupMessages.createTable();
    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    @Test
    public void whenCreatingMessageTable_thenCreatedCorrectly() {
        ResultSet result = cassandra.runQuery(
                "SELECT * FROM " + GroupMessageTable.TABLE_NAME + ";");

        List<String> columnNames =
                result.getColumnDefinitions().asList().stream()
                        .map(ColumnDefinitions.Definition::getName)
                        .collect(Collectors.toList());

        assertEquals(4, columnNames.size());
        assertTrue(columnNames.contains("chat_id"));
        assertTrue(columnNames.contains("message_id"));
        assertTrue(columnNames.contains("sender_id"));
        assertTrue(columnNames.contains("content"));
    }

    @Test
    public void whenCreatingGroupMessage_thenCreatedCorrectly() {
        GroupChatTable groupChats = new GroupChatTable(cassandra);
        groupChats.createTable();
        UUID adminId = UUID.randomUUID();
        String name = "name";
        String description = "description";
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID messageId = null;
        String content = "content";
        try {
            messageId = groupMessages.createGroupMessage(chatId, adminId, content).getMessage_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group message: " + e.getMessage());
        }

        GroupMessage createdMessage = groupMessages.getMapper().get(chatId, messageId);

        assertEquals(messageId, createdMessage.getMessage_id());
        assertEquals(adminId, createdMessage.getSender_id());
        assertEquals(content, createdMessage.getContent());


        groupChats.getMapper().delete(chatId);
        groupMessages.getMapper().delete(chatId, messageId);
    }

    @Test
    public void whenCreatingGroupMessageFromANonMember_thenFailedCorrectly() {

        GroupChatTable groupChats = new GroupChatTable(cassandra);
        groupChats.createTable();
        UUID adminId = UUID.randomUUID();
        String name = "name";
        String description = "description";
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        String content = "content";
        try {
            groupMessages.createGroupMessage(chatId, UUID.randomUUID(), content);
            fail("A nonmember was able to send a message.");
        } catch (InvalidInputException ignored) {

        }
        groupChats.getMapper().delete(chatId);
    }

    @Test
    public void whenGetDirectMessage_thenReturnedCorrectly() {
        GroupChatTable groupChats = new GroupChatTable(cassandra);
        groupChats.createTable();
        UUID adminId = UUID.randomUUID();
        String name = "name";
        String description = "description";
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        UUID messageId = null;
        String content = "content";
        try {
            messageId = groupMessages.createGroupMessage(chatId, adminId, content).getMessage_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group message: " + e.getMessage());
        }

        GroupMessage createdMessage = groupMessages.getMapper().get(chatId, messageId);

        assertEquals(messageId, createdMessage.getMessage_id());
        assertEquals(adminId, createdMessage.getSender_id());
        assertEquals(content, createdMessage.getContent());


        groupChats.getMapper().delete(chatId);
        groupMessages.getMapper().delete(chatId, messageId);
    }

    @Test
    public void whenGetDirectMessageWithANonMember_thenFailedCorrectly() {

        GroupChatTable groupChats = new GroupChatTable(cassandra);
        groupChats.createTable();
        UUID adminId = UUID.randomUUID();
        String name = "name";
        String description = "description";
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(adminId, name, description).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }
        String content = "content";
        try {
            groupMessages.createGroupMessage(chatId, adminId, content);
        } catch (InvalidInputException e) {
            fail("A member failed to send a message.");
        }
        try {
            groupMessages.getGroupMessages(chatId, UUID.randomUUID());
            fail("A nonmember was able to get a message.");
        } catch (InvalidInputException ignored) {

        }

        groupChats.getMapper().delete(chatId);
    }
}
