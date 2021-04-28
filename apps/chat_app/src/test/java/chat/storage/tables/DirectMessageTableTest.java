package chat.storage.tables;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.DirectMessage;
import org.sab.chat.storage.models.GroupMessage;
import org.sab.chat.storage.tables.DirectChatTable;
import org.sab.chat.storage.tables.DirectMessageTable;
import org.sab.chat.storage.tables.GroupChatTable;

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
    public void whenCreatingMessageTable_thenCreatedCorrectly() {
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
        UUID first_member = UUID.randomUUID();
        UUID second_member = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = directChats.createDirectChat( first_member, second_member);
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }
        UUID message_id = null;
        String content = "content";
        try {
            message_id = directMessages.createDirectMessage(chat_id,first_member,content);

        } catch (InvalidInputException e) {
            fail("Failed to create direct message: "+ e.getMessage());
        }

        DirectMessage createdMessage = directMessages.getMapper().get(chat_id,message_id);

        assertEquals(message_id,createdMessage.getMessage_id());
        assertEquals(first_member, createdMessage.getSender_id());
        assertEquals(content, createdMessage.getContent());

        directChats.getMapper().delete(chat_id);
        directMessages.getMapper().delete(chat_id, message_id);
    }

        @Test
    public void whenCreatingDirectMessageFromANonMember_thenFailedCorrectly() {

        UUID first_member = UUID.randomUUID();
        UUID second_member = UUID.randomUUID();
        DirectChatTable directChats = new DirectChatTable(cassandra);
        directChats.createTable();
        UUID chat_id = null;
        try {
            chat_id = directChats.createDirectChat( first_member, second_member);
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }
        UUID message_id = null;
        String content = "content";
        try {
            message_id = directMessages.createDirectMessage(chat_id,UUID.randomUUID(),content);
            fail("A nonmember failed to send a message");
        } catch (InvalidInputException e) {

        }
        directChats.getMapper().delete(chat_id);
    }
    @Test
    public void whenGetDirectMessage_thenReturnedCorrectly() {
        DirectChatTable directChats = new DirectChatTable(cassandra);
        directChats.createTable();
        UUID first_member = UUID.randomUUID();
        UUID second_member = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = directChats.createDirectChat(first_member, second_member);
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }
        UUID message_id = null;
        String content = "content";
        try {
            message_id = directMessages.createDirectMessage(chat_id,first_member, content);

        } catch (InvalidInputException e) {
            fail("Failed to create direct message: "+ e.getMessage());
        }

        DirectMessage createdMessage = directMessages.getMapper().get(chat_id,message_id);

        assertEquals(message_id,createdMessage.getMessage_id());
        assertEquals(first_member, createdMessage.getSender_id());
        assertEquals(content, createdMessage.getContent());


        directChats.getMapper().delete(chat_id);
        directMessages.getMapper().delete(chat_id, message_id);
    }

    @Test
    public void whenGetDirectMessageWithANonMember_thenFailedCorrectly() {

        DirectChatTable directChat = new DirectChatTable(cassandra);
        directChat.createTable();
        UUID first_member = UUID.randomUUID();
        UUID second_member = UUID.randomUUID();
        UUID chat_id = null;
        try {
            chat_id = directChat.createDirectChat(first_member, second_member);
        } catch (InvalidInputException e) {
            fail("Failed to create direct chat: " + e.getMessage());
        }
        String content = "content";
        try {
            directMessages.createDirectMessage(chat_id, first_member ,content);
        } catch (InvalidInputException e) {
            fail("A nonmember failed to send a message");
        }
        try {
            directMessages.getDirectMessages(chat_id,UUID.randomUUID());
            fail("A nonmember failed to get a message");
        } catch (InvalidInputException e) {

        }

        directChat.getMapper().delete(chat_id);
    }

}
