package org.sab.chat.storage.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.chat.storage.tables.DirectChatTable;
import org.sab.chat.storage.tables.DirectMessageTable;
import org.sab.chat.storage.tables.GroupChatTable;
import org.sab.chat.storage.tables.GroupMessageTable;

public class CassandraConnector {
    private Cluster cluster;
    private String node, keyspaceName, replicationStrategy;
    private Integer port;
    private Integer replicationFactor;
    private Session session;
    private GroupChatTable groupChatTable;
    private DirectChatTable directChatTable;
    private DirectMessageTable directMessageTable;
    private GroupMessageTable groupMessageTable;

    public CassandraConnector() {
        init();
    }

    private void init() {
        node = System.getenv("CASSANDRA_NODE");
        port = Integer.parseInt(System.getenv("CASSANDRA_PORT"));

        keyspaceName = System.getenv("KEYSPACE_NAME");
        replicationStrategy = System.getenv("REPLICATION_STRATEGY");
        replicationFactor = Integer.parseInt(System.getenv("REPLICATION_FACTOR"));
    }

    public void connect() {
        Cluster.Builder clusterBuilder = Cluster.builder().addContactPoint(node);
        if (port != null) {
            clusterBuilder.withPort(port);
        }
        cluster = clusterBuilder.build();

        session = cluster.connect();
    }

    public void initializeKeySpace() {
        KeyspaceInitializer.initializeKeyspace(session, keyspaceName, replicationStrategy, replicationFactor);
    }

    public void createTables() {
        groupChatTable = new GroupChatTable(this);
        groupChatTable.createTable();

        directChatTable = new DirectChatTable(this);
        directChatTable.createTable();

        groupMessageTable = new GroupMessageTable(this);
        groupMessageTable.createTable();

        directMessageTable = new DirectMessageTable(this);
        directMessageTable.createTable();
    }

    public ResultSet runQuery(String query) {
        return session.execute(query);
    }

    public Session getSession() {
        return this.session;
    }

    public String getKeyspaceName() {
        return this.keyspaceName;
    }

    public GroupChatTable getGroupChatTable() {
        return groupChatTable;
    }

    public DirectChatTable getDirectChatTable() {
        return directChatTable;
    }

    public DirectMessageTable getDirectMessageTable() {
        return directMessageTable;
    }

    public GroupMessageTable getGroupMessageTable() {
        return groupMessageTable;
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
