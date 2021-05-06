package org.sab.thread.commands;

import org.sab.service.validation.CommandWithVerification;

public abstract class ThreadCommand extends CommandWithVerification {
    protected static final String THREAD_NAME = "name";
    protected static final String DESCRIPTION = "description";
    protected static final String CREATOR_ID = "creatorId";
    protected static final String NUM_OF_FOLLOWERS = "numOfFollowers";
    protected static final String DATE_CREATED = "dateCreated";

    protected static final String ASSIGNER_ID = "assignerId";
    protected static final String MODERATOR_ID = "moderatorId";
}
