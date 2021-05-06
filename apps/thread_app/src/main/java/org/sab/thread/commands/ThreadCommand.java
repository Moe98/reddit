package org.sab.thread.commands;

import com.arangodb.entity.BaseEdgeDocument;
import org.sab.models.ThreadAttributes;
import org.sab.service.validation.CommandWithVerification;

public abstract class ThreadCommand extends CommandWithVerification {
    protected static final String THREAD_NAME = ThreadAttributes.THREAD_NAME.getHTTP();
    protected static final String DESCRIPTION = ThreadAttributes.DESCRIPTION.getHTTP();
    protected static final String CREATOR_ID = ThreadAttributes.CREATOR_ID.getHTTP();
    protected static final String NUM_OF_FOLLOWERS = ThreadAttributes.NUM_OF_FOLLOWERS.getHTTP();
    // TODO remove attribute
    protected static final String DATE_CREATED = ThreadAttributes.DATE_CREATED.getHTTP();

    protected static final String ASSIGNER_ID = ThreadAttributes.ASSIGNER_ID.getHTTP();
    protected static final String MODERATOR_ID = ThreadAttributes.MODERATOR_ID.getHTTP();
    protected static final String ACTION_MAKER_ID = ThreadAttributes.ACTION_MAKER_ID.getHTTP();

    protected static final String DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();


    // TODO get from env vars
    protected static final String DB_Name = "ARANGO_DB";
    protected static final String THREAD_COLLECTION_NAME = "Thread";
    protected static final String USER_COLLECTION_NAME = "User";
    protected static final String USER_THREAD_MOD_COLLECTION_NAME = "UserThreadMod";
    protected static final String USER_FOLLOW_THREAD_COLLECTION_NAME = "UserFollowThread";
    protected static final String USER_BOOKMARK_THREAD_COLLECTION_NAME = "UserBookmarkThread";


    protected final BaseEdgeDocument addEdgeFromUserToThread(String userId, String threadName, String key) {
        final String from = USER_COLLECTION_NAME + "/" + userId;
        final String to = THREAD_COLLECTION_NAME + "/" + threadName;

        return addEdgeFromToWithKey(from, to, key);
    }

    protected final BaseEdgeDocument addEdgeFromToWithKey(String from, String to, String key) {
        BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
        edgeDocument.setFrom(from);
        edgeDocument.setTo(to);
        edgeDocument.setKey(key);

        return edgeDocument;
    }
}
