package org.sab.chat.storage.config;

import com.datastax.driver.core.Session;

public class KeyspaceInitializer {

    public static void initializeKeyspace(Session session,
                                          String keyspaceName, String replicationStrategy, int replicationFactor) {
        String query = String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH " +
                "replication = {'class':'%s', 'replication_factor': %d};",
                keyspaceName, replicationStrategy, replicationFactor
        );
        session.execute(query);

        query = String.format("USE %s;", keyspaceName);
        session.execute(query);

    }

}
