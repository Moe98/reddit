package org.sab.user;


import com.arangodb.ArangoDB;
import org.sab.arango.Arango;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Service;

import java.io.IOException;

public class UserApp extends Service {
    public static void main(String[] args) throws IOException {

//        dbInit();
        new UserApp().start();
    }

    @Override
    public String getAppUriName() {
        return "user";
    }

    @Override
    public int getThreadCount() {
        return 10;
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }

    public static void dbInit() throws IOException {
        PostgresConnection.dbInit();
        arangoDbInit();
    }

    private static void arangoDbInit() {
        Arango arango = Arango.getInstance();
        ArangoDB arangoDB = arango.connect();
        String dbName = System.getenv("ARANGO_DB");
        if (!arango.databaseExists(arangoDB, dbName))
            arango.createDatabase(arangoDB, dbName);
        String collectionName = "Users";
        if (!arango.collectionExists(arangoDB, dbName, collectionName))
            arango.createCollection(arangoDB, dbName, collectionName, false);

    }
}
