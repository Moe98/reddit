package org.sab.chat.storage.config;

import com.datastax.driver.core.*;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sab.chat.storage.tables.DirectChatTable;
import org.sab.chat.storage.tables.DirectMessageTable;
import org.sab.chat.storage.tables.GroupChatTable;
import org.sab.chat.storage.tables.GroupMessageTable;
import org.sab.databases.PoolDoesNotExistException;
import org.sab.databases.PooledDatabaseClient;

public class CassandraConnector implements PooledDatabaseClient {
    public static CassandraConnector instance;
    private Cluster cluster;
    private String node, keyspaceName, replicationStrategy;
    private Integer port;
    private Integer replicationFactor;
    private Session session;
    private GroupChatTable groupChatTable;
    private DirectChatTable directChatTable;
    private DirectMessageTable directMessageTable;
    private GroupMessageTable groupMessageTable;
    private int maxPoolConnections;

    private CassandraConnector() {}

    public static CassandraConnector getInstance() {
        if(instance == null)
            instance = new CassandraConnector();
        return instance;
    }

    private void init() {
        node = System.getenv("CASSANDRA_NODE");
        port = Integer.parseInt(System.getenv("CASSANDRA_PORT"));

        keyspaceName = System.getenv("KEYSPACE_NAME");
        replicationStrategy = System.getenv("REPLICATION_STRATEGY");
        replicationFactor = Integer.parseInt(System.getenv("REPLICATION_FACTOR"));
    }

    public void connect() {
        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions
                .setMaxConnectionsPerHost( HostDistance.LOCAL, maxPoolConnections)
                .setMaxConnectionsPerHost( HostDistance.REMOTE, maxPoolConnections);
        Cluster.Builder clusterBuilder = Cluster.builder().addContactPoint(node).withPoolingOptions(poolingOptions);
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

    @Override
    public void createPool(int maxConnections) {
        this.maxPoolConnections = maxConnections;
        init();
        connect();
        initializeKeySpace();
        createTables();
    }

    @Override
    public void destroyPool() throws PoolDoesNotExistException {
        if(cluster == null || session == null)
            throw new PoolDoesNotExistException("Can't destroy pool if it does not exist");
        close();
    }

    @Override
    public void setMaxConnections(int maxConnections) throws PoolDoesNotExistException {
        this.maxPoolConnections = maxConnections;
        if(cluster == null)
            throw new PoolDoesNotExistException("Pool does not exist");
        cluster.getConfiguration().getPoolingOptions()
                .setMaxConnectionsPerHost( HostDistance.LOCAL, maxPoolConnections)
                .setMaxConnectionsPerHost( HostDistance.REMOTE, maxPoolConnections);
    }

    @Override
    public String getName() {
        return "CASSANDRA";
    }
}
