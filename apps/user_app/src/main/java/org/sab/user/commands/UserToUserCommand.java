package org.sab.user.commands;

import org.sab.models.user.UserAttributes;
import org.sab.service.validation.CommandWithVerification;

public abstract class UserToUserCommand extends CommandWithVerification {
    protected static final String ACTION_MAKER_ID = UserAttributes.ACTION_MAKER_ID.getHTTP();
    protected static final String IS_DELETED = UserAttributes.IS_DELETED.getHTTP();
    protected static final String USER_ID = UserAttributes.USER_ID.getHTTP();

    protected static final String ACTION_MAKER_ID_DB = UserAttributes.ACTION_MAKER_ID.getHTTP();
    protected static final String IS_DELETED_DB = UserAttributes.IS_DELETED.getDb();
    protected static final String USER_ID_DB = UserAttributes.USER_ID.getHTTP();

    // TODO get from env vars
    protected static final String DB_Name = "ARANGO_DB";
    protected static final String USER_COLLECTION_NAME = "User";
}