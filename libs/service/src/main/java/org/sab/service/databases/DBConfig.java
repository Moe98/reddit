package org.sab.service.databases;

import org.sab.databases.PooledDatabaseClient;

public class DBConfig {
    private int connectionCount;
    private PooledDatabaseClient client;

    public DBConfig(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public String getClientName() {
        return client == null ? "No client initialized yet" : client.getName();
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public PooledDatabaseClient getClient() {
        return client;
    }

    public void setClient(PooledDatabaseClient client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "DBConfig{" +
                "name='" + getClientName() + '\'' +
                ", connectionCount=" + connectionCount +
                ", dbClient=" + client +
                '}';
    }
}
