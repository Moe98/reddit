package org.sab.chat.storage.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CassandraConnector {
    private Cluster cluster;
    private String node, keyspaceName, replicationStrategy;
    private Integer port, replicationFactor;
    private Session session;

    public CassandraConnector() {
        init();
    }

    private void init() {
        JSONObject configJSON = null;
        try {
            configJSON = loadConfigFile();
        } catch (Exception e) {
            System.err.println("failed to load configuration file");
            e.printStackTrace();
        }
        node = (String) configJSON.get("CASSANDRA_NODE");
        port = (Integer) configJSON.get("CASSANDRA_PORT");

        keyspaceName = (String) configJSON.get("KEYSPACE_NAME");
        replicationStrategy = (String) configJSON.get("REPLICATION_STRATEGY");
        replicationFactor = (Integer) configJSON.get("REPLICATION_FACTOR");
    }

    public void connect() {
        Cluster.Builder b = Cluster.builder().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();

        session = cluster.connect();
        KeyspaceInitializer.initializeKeyspace(session, keyspaceName, replicationStrategy, replicationFactor);
    }

    public ResultSet runQuery(String query) {
        return session.execute(query);
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();
    }

    private JSONObject loadConfigFile() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject configJSON = null;

        Object obj = parser.parse(new FileReader(getClass().getClassLoader().getResource("config.development.json").getFile()));

        JSONObject jsonObject = (JSONObject) obj;
        configJSON = jsonObject;
        return configJSON;
    }

}
