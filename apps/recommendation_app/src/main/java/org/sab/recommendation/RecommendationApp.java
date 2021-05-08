package org.sab.recommendation;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.SubThread;
import org.sab.models.Thread;
import org.sab.service.Service;

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
    private Arango arango;
    private Couchbase couchbase;
    private Cluster cluster;

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

    private void dbInit() {
        try {
            arango = Arango.getInstance();
            arango.connect();
            couchbase = Couchbase.getInstance();
            cluster = couchbase.connect();

            couchbase.createBucketIfNotExists(cluster, listingsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(cluster, recommendedSubThreadsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(cluster, recommendedThreadsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(cluster, recommendedUsersBucketName, defaultRamQuota);
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
        } finally {
            if (arango != null)
                arango.disconnect();
            if (couchbase != null)
                couchbase.disconnect(cluster);
        }
    }

    public static void main(String[] args) {
        RecommendationApp app = new RecommendationApp();
        app.dbInit();
        app.start();
    }
}
