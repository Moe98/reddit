package org.sab.search;

import com.arangodb.ArangoDBException;
import org.sab.arango.Arango;
import org.sab.models.SubThread;
import org.sab.models.Thread;
import org.sab.service.Service;

public class SearchApp extends Service {
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

    @Override
    public String getAppUriName() {
        return "SEARCH";
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
            arango.createDatabaseIfNotExists(DB_NAME);
            arango.createCollectionIfNotExists(DB_NAME, THREADS_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_NAME, SUB_THREADS_COLLECTION_NAME, false);
            arango.createViewIfNotExists(DB_NAME, getViewName(THREADS_COLLECTION_NAME), THREADS_COLLECTION_NAME, new String[]{THREAD_NAME, THREAD_DESCRIPTION});
            arango.createViewIfNotExists(DB_NAME, getViewName(SUB_THREADS_COLLECTION_NAME), SUB_THREADS_COLLECTION_NAME, new String[]{SUB_THREAD_TITLE, SUB_THREAD_CONTENT});
        } catch (ArangoDBException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        dbInit();
        new SearchApp().start();
    }
}
