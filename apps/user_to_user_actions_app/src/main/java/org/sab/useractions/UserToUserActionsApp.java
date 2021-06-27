package org.sab.useractions;

import org.sab.service.Service;
import org.sab.arango.Arango;

public class UserToUserActionsApp extends Service {

    public static void main(String[] args) {
        try {
            new UserToUserActionsApp().start();
            dbInit();
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