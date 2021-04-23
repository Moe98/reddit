package org.sab.chat.storage.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.chat.storage.models.ChatTableInitializer;
import org.sab.chat.storage.models.MessageTableInitializer;

public class CassandraConnector {
    private Cluster cluster;
    private String node, keyspaceName, replicationStrategy;
    private Integer port;
    private Integer replicationFactor;
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
        port = ((Long) configJSON.get("CASSANDRA_PORT")).intValue();

        keyspaceName = (String) configJSON.get("KEYSPACE_NAME");
        replicationStrategy = (String) configJSON.get("REPLICATION_STRATEGY");
        replicationFactor = ((Long) configJSON.get("REPLICATION_FACTOR")).intValue();
    }

    public void connect() {
        Cluster.Builder clusterBuilder = Cluster.builder().addContactPoint(node);
        if (port != null) {
            clusterBuilder.withPort(port);
        }
        cluster = clusterBuilder.build();

        session = cluster.connect();

        KeyspaceInitializer.initializeKeyspace(session, keyspaceName, replicationStrategy, replicationFactor);
        ChatTableInitializer.createChatTable(session);
        MessageTableInitializer.createMessageTable(session);
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
        JSONObject configJSON = (JSONObject) parser.parse(new FileReader(getClass().getClassLoader().getResource("config.development.json").getFile()));
        return configJSON;
    }

}
