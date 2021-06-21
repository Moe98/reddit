package org.sab.thread;

import org.sab.couchbase.Couchbase;
import org.sab.models.CouchbaseBuckets;
import org.sab.service.Service;

import java.io.IOException;
import java.util.Properties;

public class ThreadApp extends Service {

    // TODO get this from config file
    private static final String THREAD_APP_QUEUE = "THREAD_APP_REQ";
    private static Couchbase couchbase;

    public static void main(String[] args) {
        try {
            startCouchbaseConnection();
            new ThreadApp().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAppUriName() {
        return "thread";
    }

    public static void startCouchbaseConnection() {
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
}
