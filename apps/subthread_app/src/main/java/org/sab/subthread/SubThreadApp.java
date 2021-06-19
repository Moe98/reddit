package org.sab.subthread;

import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.CouchbaseBuckets;
import org.sab.service.Service;

import java.util.ArrayList;

public class SubThreadApp extends Service {
    // TODO get from environment variables
    protected static final String DB_Name = System.getenv("ARANGO_DB");
    // TODO get this from config file
    private static final String THREAD_APP_QUEUE = "THREAD_APP_REQ";
    // TODO move all connection establishment here
    //  move all  DB creations here
    private static Arango arango;
    private static Couchbase couchbase;

    public static void main(String[] args) {

        try {
            startArangoConnection();
            startCouchbaseConnection();
            // TODO add collection creation here
            //        createCollections(collectionList, edgeCollectionList);
            new SubThreadApp().start();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdownGracefully();
        }
    }

    public static void startArangoConnection() {
        try {
            // TODO do we get the instance here?
            arango = Arango.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startCouchbaseConnection() {
        couchbase = Couchbase.getInstance();

        couchbase.createBucketIfNotExists(CouchbaseBuckets.COMMENTS.get(), 100);
        couchbase.createBucketIfNotExists(CouchbaseBuckets.SUBTHREADS.get(), 100);
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

    // TODO get values of next methods from config files
    @Override
    public String getAppUriName() {
        return "subThread";
    }
}
