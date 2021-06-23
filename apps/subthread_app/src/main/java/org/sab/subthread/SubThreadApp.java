package org.sab.subthread;

import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.CouchbaseBuckets;
import org.sab.service.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class SubThreadApp extends Service {
    // TODO get from environment variables
    protected static final String DB_Name = System.getenv("ARANGO_DB");
    // TODO get this from config file
    // TODO move all connection establishment here
    //  move all  DB creations here
    private static Arango arango;
    private static Couchbase couchbase;

    private static final String SUBTHREAD_APP_QUEUE = "SUBTHREAD_APP_REQ";
    public static int SUBTHREAD_LIKES_CACHING_THRESHOLD, SUBTHREAD_DISLIKES_CACHING_THRESHOLD,
            COMMENT_LIKES_CACHING_THRESHOLD, COMMENT_DISLIKES_CACHING_THRESHOLD;
    
    public static void main(String[] args) {

        try {
            dbInit();

            // TODO add collection creation here
            //        createCollections(collectionList, edgeCollectionList);
            new SubThreadApp().start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void dbInit() {
        startCouchbaseConnection();
        startArangoConnection();
    }

    static void startArangoConnection() {
        arango = Arango.getInstance();
        arango.createDatabaseIfNotExists(System.getenv("ARANGO_DB"));
    }

    public static void startCouchbaseConnection() {
        initCacheThreshold();

        couchbase = Couchbase.getInstance();
        couchbase.connectIfNotConnected();

        final Properties properties = new Properties();

        try {
            properties.load(SubThreadApp.class.getClassLoader().getResourceAsStream("configurations.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final int commentRamQuota = Integer.parseInt(properties.getProperty("COMMENT_RAM_QUOTA"));
        final int subthreadRamQuota = Integer.parseInt(properties.getProperty("SUBTHREAD_RAM_QUOTA"));

        couchbase.createBucketIfNotExists(CouchbaseBuckets.COMMENTS.get(), commentRamQuota);
        couchbase.createBucketIfNotExists(CouchbaseBuckets.RECOMMENDED_SUB_THREADS.get(), subthreadRamQuota);
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

    public static void initCacheThreshold(){
        final Properties properties = new Properties();

        int defaultVal = 1000;

        SUBTHREAD_LIKES_CACHING_THRESHOLD = defaultVal;
        SUBTHREAD_DISLIKES_CACHING_THRESHOLD = defaultVal;
        COMMENT_LIKES_CACHING_THRESHOLD = defaultVal;
        COMMENT_DISLIKES_CACHING_THRESHOLD = defaultVal;

        try {
            properties.load(SubThreadApp.class.getClassLoader().getResourceAsStream("cacheThreshold.properties"));
            SUBTHREAD_LIKES_CACHING_THRESHOLD = Integer.parseInt(properties.getProperty("SUBTHREAD_LIKES"));
            SUBTHREAD_DISLIKES_CACHING_THRESHOLD = Integer.parseInt(properties.getProperty("SUBTHREAD_DISLIKES"));
            COMMENT_LIKES_CACHING_THRESHOLD = Integer.parseInt(properties.getProperty("COMMENT_LIKES"));
            COMMENT_DISLIKES_CACHING_THRESHOLD = Integer.parseInt(properties.getProperty("COMMENT_DISLIKES"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
