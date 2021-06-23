package org.sab.useractions;

import com.arangodb.ArangoDBException;
import org.sab.service.Service;
import org.sab.arango.Arango;

public class UserToUserActionsApp extends Service {

    public static void main(String[] args) {
        try {
            dbInit();
            new UserToUserActionsApp().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void dbInit() {
        Arango arango = Arango.getInstance();
        arango.createDatabaseIfNotExists(System.getenv("ARANGO_DB"));
    }

    @Override
    public String getAppUriName() {
        return "useraction";
    }

}