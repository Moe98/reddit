package org.sab.databases;

public interface PooledDatabaseClient {

    // Note: a decorator pattern would be a better design strategy
    void createPool(int maxConnections);
    void destroyPool();
    void setMaxConnections(int maxConnections);

    /**
     * Compulsory method!
     * Every concrete class that implements this interface needs to implement this method
     * We assume that the concept of "consenting adults" apply
     *
     * ConcreteClass should be replaced by the DB client type Ex. Arango
     * */
    // public static ConcreteClass getClient()
     
}
