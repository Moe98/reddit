package org.sab.databases;

/**
 * This class assumes that the DB client is following a singleton pattern
 * However there is no clean way to force the inheritance of singletons
 *
 * Thus, we assume that the concept of "consenting adults" applies and
 * that the concrete classes will follow without an explicit contract
 *
 * Check commented compulsory method below
 * */
public interface PooledDatabaseClient {

    // Note: a decorator pattern would be a better design strategy
    void createPool(int maxConnections);
    void destroyPool() throws PoolDoesNotExistException;
    void setMaxConnections(int maxConnections) throws PoolDoesNotExistException;

    /**
     * Compulsory method!
     * Every concrete class that implements this interface needs to implement this method
     *
     * ConcreteClass should be replaced by the DB client type Ex. Arango
     * */
    // public static ConcreteClass getInstance()
     
}
