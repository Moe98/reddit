package org.sab.user;


import org.sab.arango.Arango;
import org.sab.models.user.User;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Service;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.IOException;

public class UserApp extends Service {
    public static final String ARANGO_DB_NAME = System.getenv("ARANGO_DB");

    public static void main(String[] args) throws IOException, EnvironmentVariableNotLoaded {

        dbInit();
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
        try {
            arango.connectIfNotConnected();
            arango.createDatabaseIfNotExists(ARANGO_DB_NAME);
            arango.createCollectionIfNotExists(ARANGO_DB_NAME, User.getCollectionName(), false);
        } finally {
            arango.disconnect();
        }
    }
}
