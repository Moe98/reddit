package org.sab.service.databases;

import org.sab.databases.PooledDatabaseClient;

public class DBConfig {
    private final String name;
    private int connectionCount;
    private PooledDatabaseClient client;

    public DBConfig(String name, int connectionCount) {
        this.name = name;
        this.connectionCount = connectionCount;
    }

    public DBConfig(String name, int connectionCount, PooledDatabaseClient client) {
        this.name = name;
        this.connectionCount = connectionCount;
        this.client = client;
    }

    public String getName() {
        return name;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public PooledDatabaseClient getClient() {
        return client;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public void setClient(PooledDatabaseClient client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "DBConfig{" +
                "name='" + name + '\'' +
                ", connectionCount=" + connectionCount +
                ", dbClient=" + client +
                '}';
    }
}
