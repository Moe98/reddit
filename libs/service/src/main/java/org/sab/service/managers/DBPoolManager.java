package org.sab.service.managers;

import org.sab.databases.PoolDoesNotExistException;
import org.sab.databases.PooledDatabaseClient;
import org.sab.service.ConfigMap;
import org.sab.service.ServiceConstants;
import org.sab.service.databases.DBConfig;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DBPoolManager {

    private final Map<String, DBConfig> requiredDbs;

    public DBPoolManager(Map<String, DBConfig> requiredDbs) {
        this.requiredDbs = requiredDbs;
    }

    public void initDbClasses() throws ReflectiveOperationException {

        for (final String dbClassName : requiredDbs.keySet()) {
            final DBConfig dbConfig = requiredDbs.get(dbClassName);
            System.out.println("DB className: " + dbClassName);

            final Class<?> clazz = Class.forName(dbClassName);
            System.out.println("DB class: " + clazz);

            PooledDatabaseClient clientInstance = (PooledDatabaseClient) clazz.getMethod(ServiceConstants.GET_DB_CLIENT_METHOD_NAME).invoke(null);
            System.out.println("Client Instance: " + clientInstance);
            dbConfig.setClient(clientInstance);
        }
    }

    public void initDbPool() {
        for (final DBConfig dbConfig : requiredDbs.values()) {
            final PooledDatabaseClient client = dbConfig.getClient();
            final int connectionCount = dbConfig.getConnectionCount();
            client.createPool(connectionCount);
            System.out.println("Initialized the " + dbConfig.getClientName() + " pool");
        }
    }

    public void setMaxConnectionCountForAll(int maxConnectionCount) {
        for (final String key: requiredDbs.keySet()) {
            final DBConfig dbConfig = requiredDbs.get(key);
            dbConfig.setConnectionCount(maxConnectionCount);
        }
    }

    public void setMaxDbConnectionsCount(String clientName, int maxDbConnectionsCount)
            throws PoolDoesNotExistException {
        DBConfig dbToModify = getClientByName(clientName);
        dbToModify.setConnectionCount(maxDbConnectionsCount);
        dbToModify.getClient().setMaxConnections(maxDbConnectionsCount);
    }

    public DBConfig getClientByName(String clientName) throws PoolDoesNotExistException {
        final Optional<DBConfig> possibleClient = requiredDbs.values().stream()
                .filter(d -> d.getClientName().equals(clientName)).findFirst();

        if(!possibleClient.isPresent()) {
            throw new PoolDoesNotExistException("Requested pool does not exist.");
        }

        return possibleClient.get();
    }


    public void releaseDbPools() {
        for(DBConfig dbConfig : requiredDbs.values()) {
            try {
                dbConfig.getClient().destroyPool();
            } catch (PoolDoesNotExistException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispose() {
        releaseDbPools();
    }
}