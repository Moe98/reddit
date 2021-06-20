package org.sab.recommendation;

import com.arangodb.ArangoDBException;
import com.couchbase.client.core.error.CouchbaseException;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.recommendation.commands.RecommendationCommand;
import org.sab.recommendation.commands.UpdatePopularSubThreads;
import org.sab.recommendation.commands.UpdatePopularThreads;
import org.sab.service.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecommendationApp extends Service {
    final private static int DEFAULT_RAM_QUOTA = 100;
    final private static int periodicTasksPeriod = 15;

    public static void dbInit() {
            Arango arango = Arango.getInstance();

            Couchbase couchbase = Couchbase.getInstance();
            couchbase.connectIfNotConnected();

            couchbase.createBucketIfNotExists(RecommendationCommand.LISTINGS_BUCKET_NAME, DEFAULT_RAM_QUOTA);
            couchbase.createBucketIfNotExists(RecommendationCommand.RECOMMENDED_SUB_THREADS_BUCKET_NAME, DEFAULT_RAM_QUOTA);
            couchbase.createBucketIfNotExists(RecommendationCommand.RECOMMENDED_THREADS_BUCKET_NAME, DEFAULT_RAM_QUOTA);
            couchbase.createBucketIfNotExists(RecommendationCommand.RECOMMENDED_USERS_BUCKET_NAME, DEFAULT_RAM_QUOTA);
            arango.createDatabaseIfNotExists(RecommendationCommand.DB_NAME);
            arango.createCollectionIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.THREADS_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.SUB_THREADS_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.USERS_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.USER_FOLLOW_USER_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.USER_FOLLOW_THREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.USER_BLOCK_USER_COLLECTION_NAME, true);
            arango.createViewIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.getViewName(RecommendationCommand.THREADS_COLLECTION_NAME), RecommendationCommand.THREADS_COLLECTION_NAME, new String[]{RecommendationCommand.THREAD_NAME, RecommendationCommand.THREAD_DESCRIPTION});
            arango.createViewIfNotExists(RecommendationCommand.DB_NAME, RecommendationCommand.getViewName(RecommendationCommand.SUB_THREADS_COLLECTION_NAME), RecommendationCommand.SUB_THREADS_COLLECTION_NAME, new String[]{RecommendationCommand.SUB_THREAD_TITLE, RecommendationCommand.SUB_THREAD_CONTENT});
    }

    public static void main(String[] args) {

        dbInit();
        new RecommendationApp().start();
        Runnable periodicTasks = () -> {
            new UpdatePopularThreads().execute();
            new UpdatePopularSubThreads().execute();
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(periodicTasks, 0, periodicTasksPeriod, TimeUnit.MINUTES);
    }

    @Override
    public String getAppUriName() {
        return "RECOMMENDATION";
    }
}
