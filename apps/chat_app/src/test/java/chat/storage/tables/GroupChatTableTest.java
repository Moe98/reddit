package chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.tables.GroupChatTable;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class GroupChatTableTest {

    private CassandraConnector cassandra;
    private GroupChatTable chats;

    @Before
    public void connect() {
        cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();

        chats = new GroupChatTable(cassandra);
        chats.createTable();
    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    @Test
    public void whenCreatingChatTable_thenCreatedCorrectly() {
        ResultSet result = cassandra.runQuery(
                "SELECT * FROM " + chats.TABLE_NAME + ";");

        List<String> columnNames =
                result.getColumnDefinitions().asList().stream()
                        .map(cl -> cl.getName())
                        .collect(Collectors.toList());

        assertEquals(6, columnNames.size());
        assertTrue(columnNames.contains("chat_id"));
        assertTrue(columnNames.contains("name"));
        assertTrue(columnNames.contains("description"));
        assertTrue(columnNames.contains("members"));
        assertTrue(columnNames.contains("admin"));
        assertTrue(columnNames.contains("date_created"));
    }
}
