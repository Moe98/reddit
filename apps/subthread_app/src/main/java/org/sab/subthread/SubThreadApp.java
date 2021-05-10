package org.sab.subthread;

import com.arangodb.ArangoDB;
import org.sab.arango.Arango;
import org.sab.service.Service;

import java.util.ArrayList;

public class SubThreadApp extends Service {
    private static Arango arango;
    private static ArangoDB arangoDB;
    // TODO get from environment variables
    protected static final String DB_Name = "ARANGO_DB";
    // TODO move all connection establishment here
    //  move all  DB creations here

    // TODO get this from config file
    private static final String THREAD_APP_QUEUE = "THREAD_APP_REQ";

    public static void main(String[] args) {

        try {
            startArangoConnection();
            // TODO add collection creation here
            //        createCollections(collectionList, edgeCollectionList);
            new SubThreadApp().start();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdownGracefully();
        }
    }

    // TODO get values of next methods from config files
    @Override
    public String getAppUriName() {
        return "thread";
    }

    @Override
    public int getThreadCount() {
        return 10;
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }

    public static void startArangoConnection() {
        try {
            // TODO do we get the instance here?
            arango = Arango.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdownGracefully() {
        // TODO stop threads and halt app (super call?)
    }

    public static void createCollections(ArrayList<String> collectionNames, ArrayList<String> edgeCollectionNames) {
        try {
        // TODO use a properties file
            for (String collectionName : collectionNames) {
                if (!arango.collectionExists(DB_Name, collectionName)) {
                    arango.createCollection(DB_Name, collectionName, false);
                }
            }

            for (String collectionName : edgeCollectionNames) {
                if (!arango.collectionExists(DB_Name, collectionName)) {
                    arango.createCollection(DB_Name, collectionName, true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}