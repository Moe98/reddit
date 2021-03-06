package org.sab.search;

import org.sab.arango.Arango;
import org.sab.models.CollectionNames;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.service.Service;

public class SearchApp extends Service {
    final public static String DB_NAME = System.getenv("ARANGO_DB");
    final public static String THREADS_COLLECTION_NAME = CollectionNames.THREAD.get();
    final public static String THREAD_NAME = ThreadAttributes.THREAD_NAME.getDb();
    final public static String THREAD_DESCRIPTION = ThreadAttributes.DESCRIPTION.getDb();
    final public static String THREAD_CREATOR = ThreadAttributes.CREATOR_ID.getDb();
    final public static String THREAD_FOLLOWERS = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    final public static String THREAD_DATE = ThreadAttributes.DATE_CREATED.getDb();
    final public static String SUB_THREADS_COLLECTION_NAME = CollectionNames.SUBTHREAD.get();
    final public static String SUB_THREAD_ID = SubThreadAttributes.SUBTHREAD_ID.getDb();
    final public static String SUB_THREAD_PARENT_THREAD = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    final public static String SUB_THREAD_TITLE = SubThreadAttributes.TITLE.getDb();
    final public static String SUB_THREAD_CREATOR = SubThreadAttributes.CREATOR_ID.getDb();
    final public static String SUB_THREAD_LIKES = SubThreadAttributes.LIKES.getDb();
    final public static String SUB_THREAD_DISLIKES = SubThreadAttributes.DISLIKES.getDb();
    final public static String SUB_THREAD_CONTENT = SubThreadAttributes.CONTENT.getDb();
    final public static String SUB_THREAD_HAS_IMAGE = SubThreadAttributes.HAS_IMAGE.getDb();
    final public static String SUB_THREAD_DATE = SubThreadAttributes.DATE_CREATED.getDb();
    final public static String SEARCH_KEYWORDS = "searchKeywords";

    public static String getViewName(String collectionName) {
        return collectionName + "View";
    }

    public static void dbInit() {

        Arango arango = Arango.getInstance();

        arango.createDatabaseIfNotExists(DB_NAME);
        arango.createCollectionIfNotExists(DB_NAME, THREADS_COLLECTION_NAME, false);
        arango.createCollectionIfNotExists(DB_NAME, SUB_THREADS_COLLECTION_NAME, false);
        arango.createViewIfNotExists(DB_NAME, getViewName(THREADS_COLLECTION_NAME), THREADS_COLLECTION_NAME, new String[]{"_key", THREAD_DESCRIPTION});
        arango.createViewIfNotExists(DB_NAME, getViewName(SUB_THREADS_COLLECTION_NAME), SUB_THREADS_COLLECTION_NAME, new String[]{SUB_THREAD_TITLE, SUB_THREAD_CONTENT});

    }

    public static void main(String[] args) {
        new SearchApp().start();
        dbInit();
    }

    @Override
    public String getAppUriName() {
        return "SEARCH";
    }
}
