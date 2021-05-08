package org.sab.user.commands;

import com.arangodb.entity.BaseEdgeDocument;
import org.sab.models.user.UserAttributes;
import org.sab.service.validation.CommandWithVerification;

public abstract class UserToUserCommand extends CommandWithVerification {
    protected static final String ACTION_MAKER_ID = UserAttributes.ACTION_MAKER_ID.getHTTP();
    protected static final String IS_DELETED = UserAttributes.IS_DELETED.getHTTP();
    protected static final String USER_ID = UserAttributes.USER_ID.getHTTP();
    protected static final String NUM_OF_FOLLOWERS = UserAttributes.NUM_OF_FOLLOWERS.getHTTP();

    protected static final String ACTION_MAKER_ID_DB = UserAttributes.ACTION_MAKER_ID.getDb();
    protected static final String IS_DELETED_DB = UserAttributes.IS_DELETED.getDb();
    protected static final String USER_ID_DB = UserAttributes.USER_ID.getDb();
    protected static final String NUM_OF_FOLLOWERS_DB = UserAttributes.NUM_OF_FOLLOWERS.getDb();

    // TODO get from env vars
    protected static final String DB_Name = "ARANGO_DB";
    protected static final String USER_COLLECTION_NAME = "User";
    protected static final String USER_FOLLOWS_USER_COLLECTION_NAME = "UserFollowsUser";
    protected static final String USER_BLOCK_USER_COLLECTION_NAME = "UserBlockUser";

    protected final BaseEdgeDocument addEdgeFromUserToUser(String actionMakerId, String userId) {
        final String from = USER_COLLECTION_NAME + "/" + actionMakerId;
        final String to = USER_COLLECTION_NAME + "/" + userId;

        return addEdgeFromToWithKey(from, to);
    }

    protected final BaseEdgeDocument addEdgeFromToWithKey(String from, String to) {
        BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
        edgeDocument.setFrom(from);
        edgeDocument.setTo(to);

        return edgeDocument;
    }
}