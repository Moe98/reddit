package org.sab.user;


import org.sab.arango.Arango;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Service;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.IOException;

public class UserApp extends Service {
    public static final String ARANGO_DB_NAME = System.getenv("ARANGO_DB");

    public static void main(String[] args) throws IOException, EnvironmentVariableNotLoaded {

        dbInit(false);
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

    public static void dbInit(boolean isTesting) throws IOException, EnvironmentVariableNotLoaded {
        PostgresConnection.dbInit(isTesting);
        arangoDbInit();
    }

    private static void arangoDbInit() throws EnvironmentVariableNotLoaded {
        if (ARANGO_DB_NAME == null)
            throw new EnvironmentVariableNotLoaded("ARANGO_DB");
        Arango arango = Arango.getInstance();
        arango.connectIfNotConnected();
        arango.createDatabaseIfNotExists(ARANGO_DB_NAME);
        String collectionName = "Users";
        arango.createCollectionIfNotExists(ARANGO_DB_NAME, collectionName, false);

    }
}
