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
    final public static String dbName = System.getenv("ARANGO_DB");
    final public static String threadsCollectionName = Thread.getCollectionName();
    final public static String threadName = Thread.getNameAttributeName();
    final public static String threadDescription = Thread.getDescriptionAttributeName();
    final public static String threadCreator = Thread.getCreatorAttributeName();
    final public static String threadFollowers = Thread.getNumOfFollowersAttributeName();
    final public static String threadDate = Thread.getDateCreatedAttributeName();
    final public static String subThreadsCollectionName = SubThread.getCollectionName();
    final public static String subThreadId = SubThread.getIdAttributeName();
    final public static String subThreadParentThread = SubThread.getParentThreadAttributeName();
    final public static String subThreadTitle = SubThread.getTitleAttributeName();
    final public static String subThreadCreator = SubThread.getCreatorAttributeName();
    final public static String subThreadLikes = SubThread.getLikesAttributeName();
    final public static String subThreadDislikes = SubThread.getDislikesAttributeName();
    final public static String subThreadContent = SubThread.getContentAttributeName();
    final public static String subThreadHasImage = SubThread.getHasImageAttributeName();
    final public static String subThreadDate = SubThread.getDateAttributeName();
    final public static String usersCollectionName = "Users";
    final public static String threadContainSubThreadCollectionName = "ThreadContainSubThread";
    final public static String userFollowUserCollectionName = "UserFollowUser";
    final public static String userFollowThreadCollectionName = "UserFollowThread";
    final public static String userFollowThreadDate = "Date";
    final public static int defaultRamQuota = 100;
    final public static String listingsBucketName = "Listings";
    final public static String listingsPopularThreadsKey = "popThreads";
    final public static String listingsPopularSubThreadsKey = "popSubThreads";
    final public static String recommendedSubThreadsBucketName = "RecommendedSubThreads";
    final public static String recommendedThreadsBucketName = "RecommendedThreads";
    final public static String recommendedUsersBucketName = "RecommendedUsers";
    final public static String subThreadsDataKey = "listOfSubThreads";
    final public static String threadsDataKey = "listOfThreads";
    final public static String usernamesDataKey = "listOfUsernames";

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

            couchbase.createBucketIfNotExists(listingsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(recommendedSubThreadsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(recommendedThreadsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(recommendedUsersBucketName, defaultRamQuota);
            arango.createDatabaseIfNotExists(dbName);
            arango.createCollectionIfNotExists(dbName, threadsCollectionName, false);
            arango.createCollectionIfNotExists(dbName, subThreadsCollectionName, false);
            arango.createCollectionIfNotExists(dbName, usersCollectionName, false);
            arango.createCollectionIfNotExists(dbName, threadContainSubThreadCollectionName, true);
            arango.createCollectionIfNotExists(dbName, userFollowUserCollectionName, true);
            arango.createCollectionIfNotExists(dbName, userFollowThreadCollectionName, true);
            arango.createViewIfNotExists(dbName, getViewName(threadsCollectionName), threadsCollectionName, new String[]{threadName, threadDescription});
            arango.createViewIfNotExists(dbName, getViewName(subThreadsCollectionName), subThreadsCollectionName, new String[]{subThreadTitle, subThreadContent});
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
