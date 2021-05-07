package org.sab.user;


import com.arangodb.ArangoDB;
import org.sab.arango.Arango;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Service;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.IOException;

public class UserApp extends Service {
    static final String ARANGO_DB_NAME = System.getenv("ARANGO_DB");

    public static void main(String[] args) throws IOException, EnvironmentVariableNotLoaded {

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

    public static void dbInit() throws IOException, EnvironmentVariableNotLoaded {
        PostgresConnection.dbInit();
        arangoDbInit();
    }

    private static void arangoDbInit() throws EnvironmentVariableNotLoaded {
        if (ARANGO_DB_NAME == null)
            throw new EnvironmentVariableNotLoaded("ARANGO_DB");
        Arango arango = Arango.getInstance();
        ArangoDB arangoDB = arango.connect();
        if (!arango.databaseExists(arangoDB, ARANGO_DB_NAME))
            arango.createDatabase(arangoDB, ARANGO_DB_NAME);
        String collectionName = "Users";
        if (!arango.collectionExists(arangoDB, ARANGO_DB_NAME, collectionName))
            arango.createCollection(arangoDB, ARANGO_DB_NAME, collectionName, false);

    }
}
