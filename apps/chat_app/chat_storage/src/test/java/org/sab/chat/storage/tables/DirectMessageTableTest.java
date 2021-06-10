package org.sab.chat.storage.tables;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectMessage;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

public class DirectMessageTableTest {

    private CassandraConnector cassandra;
    private DirectMessageTable directMessages;

    @Before
    public void connect() {
        cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();

        directMessages = new DirectMessageTable(cassandra);
        directMessages.createTable();

    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    @Test
    public void checkDirectMessageTableExists() {
        ResultSet result = cassandra.runQuery(
                "SELECT * FROM " + DirectMessageTable.TABLE_NAME + ";");

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
    public void whenCreatingDirectMessage_thenCreatedCorrectly() {
        DirectChatTable directChats = new DirectChatTable(cassandra);
        directChats.createTable();
        UUID firstMember = UUID.randomUUID();
        UUID secondMember = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = directChats.createDirectChat(firstMember, secondMember).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }
        UUID messageId = null;
        String content = "content";
        try {
            messageId = directMessages.createDirectMessage(chatId, firstMember, content).getMessage_id();

        } catch (InvalidInputException e) {
            fail("Failed to create direct message: " + e.getMessage());
        }

        DirectMessage createdMessage = directMessages.getMapper().get(chatId, messageId);

        assertEquals(messageId, createdMessage.getMessage_id());
        assertEquals(firstMember, createdMessage.getSender_id());
        assertEquals(content, createdMessage.getContent());

        directChats.getMapper().delete(chatId);
        directMessages.getMapper().delete(chatId, messageId);
    }

    @Test
    public void whenCreatingDirectMessageFromANonMember_thenFailedCorrectly() {

        UUID firstMember = UUID.randomUUID();
        UUID secondMember = UUID.randomUUID();
        DirectChatTable directChats = new DirectChatTable(cassandra);
        directChats.createTable();
        UUID chatId = null;
        try {
            chatId = directChats.createDirectChat(firstMember, secondMember).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }
        String content = "content";
        try {
            directMessages.createDirectMessage(chatId, UUID.randomUUID(), content);
            fail("A nonmember failed to send a message");
        } catch (InvalidInputException ignored) {

        }
        directChats.getMapper().delete(chatId);
    }

    @Test
    public void whenGetDirectMessage_thenReturnedCorrectly() {
        DirectChatTable directChats = new DirectChatTable(cassandra);
        directChats.createTable();
        UUID firstMember = UUID.randomUUID();
        UUID secondMember = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = directChats.createDirectChat(firstMember, secondMember).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }
        UUID messageId = null;
        String content = "content";
        try {
            messageId = directMessages.createDirectMessage(chatId, firstMember, content).getMessage_id();
        } catch (InvalidInputException e) {
            fail("Failed to create direct message: " + e.getMessage());
        }

        DirectMessage createdMessage = directMessages.getMapper().get(chatId, messageId);

        assertEquals(messageId, createdMessage.getMessage_id());
        assertEquals(firstMember, createdMessage.getSender_id());
        assertEquals(content, createdMessage.getContent());


        directChats.getMapper().delete(chatId);
        directMessages.getMapper().delete(chatId, messageId);
    }

    @Test
    public void whenGetDirectMessageWithANonMember_thenFailedCorrectly() {

        DirectChatTable directChat = new DirectChatTable(cassandra);
        directChat.createTable();
        UUID firstMember = UUID.randomUUID();
        UUID secondMember = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = directChat.createDirectChat(firstMember, secondMember).getChat_id();
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }
        String content = "content";
        try {
            directMessages.createDirectMessage(chatId, firstMember, content);
        } catch (InvalidInputException e) {
            fail("A nonmember failed to send a message");
        }
        try {
            directMessages.getDirectMessages(chatId, UUID.randomUUID());
            fail("A nonmember failed to get a message");
        } catch (InvalidInputException ignored) {

        }

        directChat.getMapper().delete(chatId);
    }

}
