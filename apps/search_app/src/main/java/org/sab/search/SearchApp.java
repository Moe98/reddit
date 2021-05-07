package org.sab.search;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import org.sab.arango.Arango;
import org.sab.models.SubThread;
import org.sab.service.Service;
import org.sab.models.Thread;

public class SearchApp extends Service {
    private Arango arango;
    private ArangoDB arangoDB;
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
    final public static String subThreadTime = SubThread.getDateAttributeName();

    @Override
    public String getAppUriName() {
        return "SEARCH";
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
            arangoDB = arango.connect();
            arango.createDatabaseIfNotExists(arangoDB, dbName);
            arango.createCollectionIfNotExists(arangoDB, dbName, threadsCollectionName, false);
            arango.createCollectionIfNotExists(arangoDB, dbName, subThreadsCollectionName, false);
            arango.createViewIfNotExists(arangoDB, dbName, getViewName(threadsCollectionName), threadsCollectionName, new String[]{threadName, threadDescription});
            arango.createViewIfNotExists(arangoDB, dbName, getViewName(subThreadsCollectionName), subThreadsCollectionName, new String[]{subThreadTitle, subThreadContent});
        } catch (ArangoDBException e) {
            e.printStackTrace();
        } finally {
            arango.disconnect(arangoDB);
        }
    }

    public static void main(String[] args) {
        SearchApp app = new SearchApp();
        app.dbInit();
        app.start();
    }
}
