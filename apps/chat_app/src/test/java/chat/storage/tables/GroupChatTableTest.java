package chat.storage.tables;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.exceptions.InvalidInputException;
import org.sab.chat.storage.models.GroupChat;
import org.sab.chat.storage.tables.GroupChatTable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


public class GroupChatTableTest {

    private CassandraConnector cassandra;
    private GroupChatTable groupChats;

    @Before
    public void connect() {
        cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();

        groupChats = new GroupChatTable(cassandra);
        groupChats.createTable();
    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    @Test
    public void whenCreatingChatTable_thenCreatedCorrectly() {
        ResultSet result = cassandra.runQuery(
                "SELECT * FROM " + GroupChatTable.TABLE_NAME + ";");

        List<String> columnNames =
                result.getColumnDefinitions().asList().stream()
                        .map(ColumnDefinitions.Definition::getName)
                        .collect(Collectors.toList());

        assertEquals(6, columnNames.size());
        assertTrue(columnNames.contains("chat_id"));
        assertTrue(columnNames.contains("name"));
        assertTrue(columnNames.contains("description"));
        assertTrue(columnNames.contains("members"));
        assertTrue(columnNames.contains("admin"));
        assertTrue(columnNames.contains("date_created"));
    }

    @Test
    public void whenCreatingGroupChat_thenCreatedCorrectly() {
        String groupName = "Alpha Squad";
        String groupDesc = "Aqwa Squad fe Masr";
        UUID admin = UUID.randomUUID();
        UUID chatId = null;
        try {
            chatId = groupChats.createGroupChat(admin, groupName, groupDesc);
        } catch (InvalidInputException e) {
            fail("Failed to create group chat: " + e.getMessage());
        }

        GroupChat createdGroupChat = groupChats.getMapper().get(chatId);

        assertEquals(groupName, createdGroupChat.getName());
        assertEquals(groupDesc, createdGroupChat.getDescription());
        assertEquals(admin, createdGroupChat.getAdmin());
        groupChats.getMapper().delete(chatId);
    }

    @Test
    public void whenCreatingGroupChatWithInvalidData_thenFailsToCreate() {
        String[] groupNames = {"Alpha Squad", "", null};
        String[] groupDescs = {null, "Aqwa Squad fe Masr", "Cats"};
        for (int i = 0; i < 3; i++) {
            UUID admin = UUID.randomUUID();
            try {
                groupChats.createGroupChat(admin, groupNames[i], groupDescs[i]);
                fail("Created group chat with invalid data");
            } catch (InvalidInputException ignored) {

            }
        }
    }

}
