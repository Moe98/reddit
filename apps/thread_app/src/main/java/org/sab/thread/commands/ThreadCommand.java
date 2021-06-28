package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.*;
import org.sab.couchbase.Couchbase;
import org.sab.models.Thread;
import org.sab.models.user.UserAttributes;
import org.sab.service.validation.CommandWithVerification;

public abstract class ThreadCommand extends CommandWithVerification {
    // thread attributes
    protected static final String THREAD_NAME = ThreadAttributes.THREAD_NAME.getHTTP();
    protected static final String DESCRIPTION = ThreadAttributes.DESCRIPTION.getHTTP();
    protected static final String CREATOR_ID = ThreadAttributes.CREATOR_ID.getHTTP();
    protected static final String NUM_OF_FOLLOWERS = ThreadAttributes.NUM_OF_FOLLOWERS.getHTTP();

    protected static final String MODERATOR_ID = ThreadAttributes.MODERATOR_ID.getHTTP();
    protected static final String BANNED_USER_ID = ThreadAttributes.BANNED_USER_ID.getHTTP();

    protected static final String THREAD_NAME_DB = ThreadAttributes.THREAD_NAME.getDb();
    protected static final String DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();

    // user attributes
    protected static final String USERNAME = UserAttributes.USERNAME.toString();
    protected static final String IS_DELETED_DB = UserAttributes.IS_DELETED.getArangoDb();

    // subthread attributes
    protected static final String SUBTHREAD_ID_DB = SubThreadAttributes.SUBTHREAD_ID.getDb();
    protected static final String PARENT_THREAD_ID_DB = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    // comment attributes
    protected static final String COMMENT_ID_DB = CommentAttributes.COMMENT_ID.getDb();
    protected static final String PARENT_SUBTHREAD_ID_DB = CommentAttributes.PARENT_SUBTHREAD_ID.getDb();

    // Messages
    protected static final String OBJECT_NOT_FOUND = "The data you are requesting does not exist.";
    protected static final String REQUESTER_NOT_AUTHOR = "You are not the author of this comment";
    protected static final String THREAD_DOES_NOT_EXIST = "This thread does not exist.";
    protected static final String USER_DOES_NOT_EXIST = "The user you are trying to ban does not exist.";
    protected static final String FOLLOWED_THREAD_SUCCESSFULLY = "You are now following this Thread!";
    protected static final String UNFOLLOWED_THREAD_SUCCESSFULLY = "You have unfollowed this Thread.";
    protected static final String BOOKMARKED_THREAD_SUCCESSFULLY = "You have added this Thread to your bookmarks!";
    protected static final String UNBOOKMARKED_THREAD_SUCCESSFULLY = "You have removed this Thread from your bookmarks.";
    protected static final String NOT_A_MODERATOR = "You are not a moderator of this thread.";
    protected static final String USER_ALREADY_BANNED = "User is already banned from this thread.";
    protected static final String USER_BANNED_SUCCESSFULLY = "User has been successfully banned.";

    protected static final String DB_Name = System.getenv("ARANGO_DB");
    //TODO: use diff db for testing
    protected static final String TEST_DB_Name = DB_Name;
    protected static final String THREAD_COLLECTION_NAME = CollectionNames.THREAD.get();
    protected static final String USER_COLLECTION_NAME = CollectionNames.USER.get();
    protected static final String USER_MOD_THREAD_COLLECTION_NAME = CollectionNames.USER_MOD_THREAD.get();
    protected static final String USER_FOLLOW_THREAD_COLLECTION_NAME = CollectionNames.USER_FOLLOW_THREAD.get();
    protected static final String USER_BOOKMARK_THREAD_COLLECTION_NAME = CollectionNames.USER_BOOKMARK_THREAD.get();
    protected static final String USER_BANNED_FROM_THREAD_COLLECTION_NAME = CollectionNames.USER_BANNED_FROM_THREAD.get();

    protected static final String SUBTHREAD_COLLECTION_NAME = CollectionNames.SUBTHREAD.get();
    protected static final String COMMENT_COLLECTION_NAME = CollectionNames.COMMENT.get();

    // TODO: remove hardcoded strings about the recommendation app
    protected static final String RECOMMENDATION_REQUEST_QUEUE = "RECOMMENDATION_REQ";
    protected static final String UPDATE_RECOMMENDED_THREADS_FUNCTION_NAME = "UPDATE_RECOMMENDED_THREADS";
    protected static final String UPDATE_RECOMMENDED_SUBTHREADS_FUNCTION_NAME = "UPDATE_RECOMMENDED_SUBTHREADS";

    // TODO get queueName from somewhere instead of hardcoding it
    protected static final String Notification_Queue_Name = "NOTIFICATION_REQ";
    // TODO get function name from somewhere consistent
    protected static final String SEND_NOTIFICATION_FUNCTION_NAME = "SEND_NOTIFICATION";

    protected final BaseEdgeDocument addEdgeFromUserToThread(String userId, String threadName) {
        final String from = USER_COLLECTION_NAME + "/" + userId;
        final String to = THREAD_COLLECTION_NAME + "/" + threadName;

        return addEdgeFromToWithKey(from, to);
    }

    protected final BaseEdgeDocument addEdgeFromToWithKey(String from, String to) {
        BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
        edgeDocument.setFrom(from);
        edgeDocument.setTo(to);

        return edgeDocument;
    }

    protected final boolean checkUserExists(Arango arango, String userId) {
        boolean userExists;
        if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
            userExists = false;
        } else {
            // TODO change to query
            BaseDocument res = arango.readDocument(DB_Name, USER_COLLECTION_NAME, userId);
            boolean isDeleted = (Boolean) res.getAttribute(IS_DELETED_DB);
            userExists = !isDeleted;
        }
        return userExists;
    }

    protected final JSONObject baseDocumentToJson(BaseDocument document) {
        Thread thread;

        final String threadName = document.getKey();
        final String creatorId = (String) document.getAttribute(CREATOR_ID_DB);
        final String description = (String) document.getAttribute(DESCRIPTION_DB);
        final int numOfFollowers = Integer.parseInt(String.valueOf(document.getAttribute(NUM_OF_FOLLOWERS_DB)));
        final String date = (String) document.getAttribute(DATE_CREATED_DB);

        thread = Thread.createNewThread(threadName, creatorId, description) ;
        thread.setDateCreated(date);
        thread.setNumOfFollowers(numOfFollowers);

        return thread.toJSON();
    }

    protected final boolean existsInCouchbase(String key) {
        return Couchbase.getInstance().documentExists(CouchbaseBuckets.RECOMMENDED_THREADS.get(), key);
    }

    protected final boolean existsInArango(String collectionName, String key) {
        return Arango.getInstance().documentExists(DB_Name, collectionName, key);
    }

    protected final void deleteDocumentFromCouchbase(String bucketName, String key) {
        Couchbase.getInstance().deleteDocumentIfExists(bucketName, key);
    }

    protected final BaseDocument getDocumentFromCouchbase(String bucketName, String key) {
        JSONObject thread = Couchbase.getInstance().getDocumentJson(bucketName, key);

        BaseDocument myObject = new BaseDocument();

        myObject.setKey(String.valueOf(thread.get(THREAD_NAME_DB)));
        myObject.addAttribute(DESCRIPTION_DB, thread.get(DESCRIPTION_DB));
        myObject.addAttribute(CREATOR_ID_DB, thread.get(CREATOR_ID_DB));
        myObject.addAttribute(DATE_CREATED_DB, thread.get(DATE_CREATED_DB));
        myObject.addAttribute(NUM_OF_FOLLOWERS_DB, thread.get(NUM_OF_FOLLOWERS_DB));

        return myObject;
    }

    protected final void replaceDocumentFromCouchbase(String bucketName, String key, BaseDocument updatedDoc) {
        Couchbase.getInstance().replaceDocument(bucketName, key, baseDocumentToJson(updatedDoc));
    }

    protected final void upsertDocumentFromCouchbase(String bucketName, String key, BaseDocument updatedDoc) {
        Couchbase.getInstance().upsertDocument(bucketName, key, baseDocumentToJson(updatedDoc));
    }
}
