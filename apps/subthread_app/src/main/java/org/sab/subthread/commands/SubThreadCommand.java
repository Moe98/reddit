package org.sab.subthread.commands;

import org.sab.service.validation.CommandWithVerification;

public abstract class SubThreadCommand extends CommandWithVerification {
    protected static final String PARENT_THREAD_ID = "parentThreadId";
    protected static final String ID = "Id";
    protected static final String TITLE = "title";
    protected static final String CREATOR_ID = "creatorId";
    protected static final String LIKES = "likes";
    protected static final String DISLIKES = "dislikes";
    protected static final String CONTENT = "content";
    protected static final String HASIMAGE = "hasImage";
    protected static final String DATE_CREATED = "dateCreated";
}
