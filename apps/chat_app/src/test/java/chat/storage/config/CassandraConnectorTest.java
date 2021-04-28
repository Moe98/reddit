package chat.storage.config;

import com.datastax.driver.core.ResultSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CassandraConnectorTest {
    private CassandraConnector cassandra;

    @Before
    public void connect() {
        cassandra = new CassandraConnector();
        cassandra.connect();
        cassandra.initializeKeySpace();
    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    @Test
    public void whenCreatingKeyspace_thenCreated() {
        String keyspaceName = cassandra.getKeyspaceName();

        ResultSet result =
                cassandra.runQuery("SELECT * FROM system_schema.keyspaces;");

        List<String> matchedKeyspaces = result.all()
                .stream()
                .filter(row -> row.getString(0).equals(keyspaceName.toLowerCase()))
                .map(row -> row.getString(0))
                .collect(Collectors.toList());

        assertEquals(1, matchedKeyspaces.size());
        assertTrue(matchedKeyspaces.get(0).equals(keyspaceName.toLowerCase()));
    }

}
