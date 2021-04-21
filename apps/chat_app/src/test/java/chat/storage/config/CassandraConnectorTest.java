package chat.storage.config;

import com.datastax.driver.core.ResultSet;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sab.chat.storage.config.CassandraConnector;

import java.io.FileReader;
import java.io.IOException;
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
    }

    @After
    public void disconnect() {
        cassandra.close();
    }

    private JSONObject loadConfigFile() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject configJSON = null;

        Object obj = parser.parse(new FileReader(getClass().getClassLoader().getResource("config.development.json").getFile()));
        JSONObject jsonObject = (JSONObject) obj;
        configJSON = jsonObject;
        return configJSON;
    }

    @Test
    public void whenCreatingAKeyspace_thenCreated() throws IOException, ParseException {
        String keyspaceName = (String) loadConfigFile().get("KEYSPACE_NAME");

        ResultSet result =
                cassandra.runQuery("SELECT * FROM system_schema.keyspaces;");

        List<String> matchedKeyspaces = result.all()
                .stream()
                .filter(r -> r.getString(0).equals(keyspaceName.toLowerCase()))
                .map(r -> r.getString(0))
                .collect(Collectors.toList());

        assertEquals(matchedKeyspaces.size(), 1);
        assertTrue(matchedKeyspaces.get(0).equals(keyspaceName.toLowerCase()));
    }

}
