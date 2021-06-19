package org.sab.recommendation.commands;

import org.sab.models.*;
import org.sab.models.user.UserAttributes;
import org.sab.service.validation.CommandWithVerification;

public abstract class RecommendationCommand extends CommandWithVerification {
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
    final public static String USERS_COLLECTION_NAME = CollectionNames.USER.get();
    final public static String THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME = CollectionNames.THREAD_CONTAIN_SUBTHREAD.get();
    final public static String USER_FOLLOW_USER_COLLECTION_NAME = CollectionNames.USER_FOLLOW_USER.get();
    final public static String USER_FOLLOW_THREAD_COLLECTION_NAME = CollectionNames.USER_FOLLOW_THREAD.get();
    final public static String USER_FOLLOW_THREAD_DATE = EdgeCollectionsAttributes.USER_FOLLOW_THREAD_DATE.getDb();
    final public static String LISTINGS_BUCKET_NAME = CouchbaseBuckets.LISTINGS.get();
    final public static String LISTINGS_POPULAR_THREADS_KEY = "popThreads";
    final public static String LISTINGS_POPULAR_SUB_THREADS_KEY = "popSubThreads";
    final public static String RECOMMENDED_SUB_THREADS_BUCKET_NAME = CouchbaseBuckets.RECOMMENDED_SUB_THREADS.get();
    final public static String RECOMMENDED_THREADS_BUCKET_NAME = CouchbaseBuckets.RECOMMENDED_THREADS.get();
    final public static String RECOMMENDED_USERS_BUCKET_NAME = CouchbaseBuckets.RECOMMENDED_USERS.get();
    final public static String SUB_THREADS_DATA_KEY = "listOfSubThreads";
    final public static String THREADS_DATA_KEY = "listOfThreads";
    final public static String USERNAMES_DATA_KEY = "listOfUsernames";
    final public static String USERNAME = UserAttributes.USERNAME.toString();
    final public static String USER_BLOCK_USER_COLLECTION_NAME = CollectionNames.USER_BLOCK_USER.get();

    public static String getViewName(String collectionName) {
        return collectionName + "View";
    }
}
