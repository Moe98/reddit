package org.sab.recommendation;

import com.arangodb.ArangoDBException;
import com.couchbase.client.core.error.CouchbaseException;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.SubThread;
import org.sab.models.Thread;
import org.sab.recommendation.commands.UpdatePopularSubThreads;
import org.sab.recommendation.commands.UpdatePopularThreads;
import org.sab.service.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecommendationApp extends Service {
    final public static String DB_NAME = System.getenv("ARANGO_DB");
    final public static String THREADS_COLLECTION_NAME = Thread.getCollectionName();
    final public static String THREAD_NAME = Thread.getNameAttributeName();
    final public static String THREAD_DESCRIPTION = Thread.getDescriptionAttributeName();
    final public static String THREAD_CREATOR = Thread.getCreatorAttributeName();
    final public static String THREAD_FOLLOWERS = Thread.getNumOfFollowersAttributeName();
    final public static String THREAD_DATE = Thread.getDateCreatedAttributeName();
    final public static String SUB_THREADS_COLLECTION_NAME = SubThread.getCollectionName();
    final public static String SUB_THREAD_ID = SubThread.getIdAttributeName();
    final public static String SUB_THREAD_PARENT_THREAD = SubThread.getParentThreadAttributeName();
    final public static String SUB_THREAD_TITLE = SubThread.getTitleAttributeName();
    final public static String SUB_THREAD_CREATOR = SubThread.getCreatorAttributeName();
    final public static String SUB_THREAD_LIKES = SubThread.getLikesAttributeName();
    final public static String SUB_THREAD_DISLIKES = SubThread.getDislikesAttributeName();
    final public static String SUB_THREAD_CONTENT = SubThread.getContentAttributeName();
    final public static String SUB_THREAD_HAS_IMAGE = SubThread.getHasImageAttributeName();
    final public static String SUB_THREAD_DATE = SubThread.getDateAttributeName();
    final public static String USERS_COLLECTION_NAME = "Users";
    final public static String THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME = "ThreadContainSubThread";
    final public static String USER_FOLLOW_USER_COLLECTION_NAME = "UserFollowUser";
    final public static String USER_FOLLOW_THREAD_COLLECTION_NAME = "UserFollowThread";
    final public static String USER_FOLLOW_THREAD_DATE = "Date";
    final public static int DEFAULT_RAM_QUOTA = 100;
    final public static String LISTINGS_BUCKET_NAME = "Listings";
    final public static String LISTINGS_POPULAR_THREADS_KEY = "popThreads";
    final public static String LISTINGS_POPULAR_SUB_THREADS_KEY = "popSubThreads";
    final public static String RECOMMENDED_SUB_THREADS_BUCKET_NAME = "RecommendedSubThreads";
    final public static String RECOMMENDED_THREADS_BUCKET_NAME = "RecommendedThreads";
    final public static String RECOMMENDED_USERS_BUCKET_NAME = "RecommendedUsers";
    final public static String SUB_THREADS_DATA_KEY = "listOfSubThreads";
    final public static String THREADS_DATA_KEY = "listOfThreads";
    final public static String USERNAMES_DATA_KEY = "listOfUsernames";
    final public static String AUTHENTICATION_PARAMS = "authenticationParams";
    final public static String AUTHENTICATED = "isAuthenticated";

    @Override
    public String getAppUriName() {
        return "RECOMMENDATION";
    }

    @Override
    public int getThreadCount() {
        return 10;
    }

    @Override
    public String getConfigMapPath() {
        return DEFAULT_PROPERTIES_FILENAME;
    }

    public static String getViewName(String collectionName) {
        return collectionName + "View";
    }

    public static void dbInit() {
        try {
            Arango arango = Arango.getInstance();
            arango.connectIfNotConnected();
            Couchbase couchbase = Couchbase.getInstance();
            couchbase.connectIfNotConnected();

            couchbase.createBucketIfNotExists(LISTINGS_BUCKET_NAME, DEFAULT_RAM_QUOTA);
            couchbase.createBucketIfNotExists(RECOMMENDED_SUB_THREADS_BUCKET_NAME, DEFAULT_RAM_QUOTA);
            couchbase.createBucketIfNotExists(RECOMMENDED_THREADS_BUCKET_NAME, DEFAULT_RAM_QUOTA);
            couchbase.createBucketIfNotExists(RECOMMENDED_USERS_BUCKET_NAME, DEFAULT_RAM_QUOTA);
            arango.createDatabaseIfNotExists(DB_NAME);
            arango.createCollectionIfNotExists(DB_NAME, THREADS_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_NAME, SUB_THREADS_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_NAME, USERS_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_NAME, THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_NAME, USER_FOLLOW_USER_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_NAME, USER_FOLLOW_THREAD_COLLECTION_NAME, true);
            arango.createViewIfNotExists(DB_NAME, getViewName(THREADS_COLLECTION_NAME), THREADS_COLLECTION_NAME, new String[]{THREAD_NAME, THREAD_DESCRIPTION});
            arango.createViewIfNotExists(DB_NAME, getViewName(SUB_THREADS_COLLECTION_NAME), SUB_THREADS_COLLECTION_NAME, new String[]{SUB_THREAD_TITLE, SUB_THREAD_CONTENT});
        } catch (ArangoDBException | CouchbaseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Runnable periodicTasks = () -> {
            new UpdatePopularThreads().execute(null);
            new UpdatePopularSubThreads().execute(null);
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(periodicTasks, 0, 15, TimeUnit.MINUTES);
        dbInit();
        new RecommendationApp().start();
    }
}
