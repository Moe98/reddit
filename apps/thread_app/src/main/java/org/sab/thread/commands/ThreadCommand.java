package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.sab.arango.Arango;
import org.sab.models.CommentAttributes;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.service.validation.CommandWithVerification;

public abstract class ThreadCommand extends CommandWithVerification {
    // thread attributes
    protected static final String THREAD_NAME = ThreadAttributes.THREAD_NAME.getHTTP();
    protected static final String DESCRIPTION = ThreadAttributes.DESCRIPTION.getHTTP();
    protected static final String CREATOR_ID = ThreadAttributes.CREATOR_ID.getHTTP();
    protected static final String NUM_OF_FOLLOWERS = ThreadAttributes.NUM_OF_FOLLOWERS.getHTTP();
    // TODO remove attribute
    protected static final String DATE_CREATED = ThreadAttributes.DATE_CREATED.getHTTP();

    protected static final String ASSIGNER_ID = ThreadAttributes.ASSIGNER_ID.getHTTP();
    protected static final String MODERATOR_ID = ThreadAttributes.MODERATOR_ID.getHTTP();
    protected static final String ACTION_MAKER_ID = ThreadAttributes.ACTION_MAKER_ID.getHTTP();
    protected static final String BANNED_USER_ID = ThreadAttributes.BANNED_USER_ID.getHTTP();

    protected static final String DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();

    // user attributes
    // TODO get from enum
    protected static final String USER_ID = UserAttributes.USER_ID.getHTTP();
    protected static final String IS_DELETED_DB = UserAttributes.IS_DELETED.getArangoDb();

    // subthread attributes
    protected static final String SUBTHREAD_ID_DB = SubThreadAttributes.SUBTHREAD_ID.getDb();
    protected static final String PARENT_THREAD_ID_DB = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    protected static final String SUBTHREAD_TITLE_DB = SubThreadAttributes.TITLE.getDb();
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
    protected static final String THREAD_COLLECTION_NAME = "Thread";
    protected static final String USER_COLLECTION_NAME = "User";
    protected static final String USER_MOD_THREAD_COLLECTION_NAME = "UserModThread";
    protected static final String USER_FOLLOW_THREAD_COLLECTION_NAME = "UserFollowThread";
    protected static final String USER_BOOKMARK_THREAD_COLLECTION_NAME = "UserBookmarkThread";
    protected static final String USER_BANNED_FROM_THREAD_COLLECTION_NAME = "UserBannedFromThread";

    protected static final String SUBTHREAD_COLLECTION_NAME = "Subthread";
    protected static final String COMMENT_COLLECTION_NAME = "Comment";


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
}
