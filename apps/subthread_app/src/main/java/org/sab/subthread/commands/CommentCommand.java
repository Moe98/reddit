package org.sab.subthread.commands;

import org.sab.service.validation.CommandWithVerification;

public abstract class CommentCommand extends CommandWithVerification {
    // TODO rename |PARENT_SUBTHREAD_ID| to |PARENT_CONTENT_ID|.
    protected static final String PARENT_SUBTHREAD_ID = "parentSubthreadId";
    protected static final String CREATOR_ID = "creatorId";
    protected static final String LIKES = "likes";
    protected static final String DISLIKES = "dislikes";
    protected static final String CONTENT = "content";
    protected static final String DATE_CREATED = "dateCreated";
    protected static final String COMMENT_ID = "commentId";
    protected static final String USER_ID = "userId";
    protected static final String PARENT_CONTENT_TYPE = "parentContentType";
}
