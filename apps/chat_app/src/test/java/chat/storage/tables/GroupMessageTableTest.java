package chat.storage.tables;

import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;
import org.sab.chat.storage.tables.GroupMessageTable;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class GroupMessageTableTest {

    private CassandraConnector cassandra;
    private GroupMessageTable messages;

    @Before
    public void connect() {
        cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();

        messages = new GroupMessageTable(cassandra);
        messages.createTable();
    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    @Test
    public void whenCreatingMessageTable_thenCreatedCorrectly() {
        ResultSet result = cassandra.runQuery(
                "SELECT * FROM " + messages.TABLE_NAME + ";");

        List<String> columnNames =
                result.getColumnDefinitions().asList().stream()
                        .map(cl -> cl.getName())
                        .collect(Collectors.toList());

        assertEquals(4, columnNames.size());
        assertTrue(columnNames.contains("chat_id"));
        assertTrue(columnNames.contains("message_id"));
        assertTrue(columnNames.contains("sender_id"));
        assertTrue(columnNames.contains("content"));
    }
}
