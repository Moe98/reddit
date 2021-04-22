package org.sab.chat.storage.config;

import com.datastax.driver.core.Session;

public class KeyspaceInitializer {

    public static void initializeKeyspace(Session session,
                                          String keyspaceName, String replicationStrategy, int replicationFactor) {
        StringBuilder query =
                new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
                        .append(keyspaceName).append(" WITH replication = {")
                        .append("'class':'").append(replicationStrategy)
                        .append("','replication_factor':").append(replicationFactor)
                        .append("};");

        session.execute(query.toString());

        query = new StringBuilder("USE ");
        query.append(keyspaceName).append(";");
        session.execute(query.toString());

    }

}
