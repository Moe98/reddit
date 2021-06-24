package org.sab.thread;

import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.CouchbaseBuckets;
import org.sab.service.Service;

import java.io.IOException;
import java.util.Properties;

public class ThreadApp extends Service {

    // TODO get this from config file
    private static final String THREAD_APP_QUEUE = "THREAD_APP_REQ";
    private static Couchbase couchbase;
    public static int THREAD_FOLLOWERS_CACHING_THRESHOLD;

    public static void main(String[] args) {
        try {
            dbInit();
            new ThreadApp().start();
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
        Arango arango = Arango.getInstance();
        arango.createDatabaseIfNotExists(System.getenv("ARANGO_DB"));
    }

    @Override
    public String getAppUriName() {
        return "thread";
    }

    public static void startCouchbaseConnection() {
        initCacheThreshold();

        couchbase = Couchbase.getInstance();
        couchbase.connectIfNotConnected();

        final Properties properties = new Properties();

        try {
            properties.load(ThreadApp.class.getClassLoader().getResourceAsStream("configurations.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final int threadRamQuota = Integer.parseInt(properties.getProperty("THREAD_RAM_QUOTA"));

        couchbase.createBucketIfNotExists(CouchbaseBuckets.RECOMMENDED_THREADS.get(), threadRamQuota);
    }

    public static void initCacheThreshold(){
        final Properties properties = new Properties();
        int defaultVal = 1000;
        THREAD_FOLLOWERS_CACHING_THRESHOLD = defaultVal;

        try {
            properties.load(ThreadApp.class.getClassLoader().getResourceAsStream("cacheThreshold.properties"));
            THREAD_FOLLOWERS_CACHING_THRESHOLD = Integer.parseInt(properties.getProperty("THREAD_FOLLOWERS"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
