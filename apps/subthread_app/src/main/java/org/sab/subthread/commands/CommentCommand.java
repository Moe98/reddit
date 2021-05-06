package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.CommentAttributes;
import org.sab.service.validation.CommandWithVerification;

import java.util.HashMap;
import java.util.Map;

public abstract class CommentCommand extends CommandWithVerification {

    // TODO rename |PARENT_SUBTHREAD_ID| to |PARENT_CONTENT_ID|.
    protected static final String PARENT_SUBTHREAD_ID = CommentAttributes.PARENT_SUBTHREAD_ID.getHTTP();
    protected static final String CREATOR_ID = CommentAttributes.CREATOR_ID.getHTTP();
    protected static final String LIKES = CommentAttributes.LIKES.getHTTP();
    protected static final String DISLIKES = CommentAttributes.DISLIKES.getHTTP();
    protected static final String CONTENT = CommentAttributes.CONTENT.getHTTP();
    //    protected static final String DATE_CREATED = "dateCreated";
    protected static final String COMMENT_ID = CommentAttributes.COMMENT_ID.getHTTP();
    protected static final String PARENT_CONTENT_TYPE = CommentAttributes.PARENT_CONTENT_TYPE.getHTTP();

    protected static final String PARENT_SUBTHREAD_ID_DB = CommentAttributes.PARENT_SUBTHREAD_ID.getDb();
    protected static final String CREATOR_ID_DB = CommentAttributes.CREATOR_ID.getDb();
    protected static final String LIKES_DB = CommentAttributes.LIKES.getDb();
    protected static final String DISLIKES_DB = CommentAttributes.DISLIKES.getDb();
    protected static final String CONTENT_DB = CommentAttributes.CONTENT.getDb();
    protected static final String DATE_CREATED_DB = CommentAttributes.DATE_CREATED.getDb();
    protected static final String COMMENT_ID_DB = CommentAttributes.COMMENT_ID.getDb();
    protected static final String PARENT_CONTENT_TYPE_DB = CommentAttributes.PARENT_CONTENT_TYPE.getDb();

    protected static final String ACTION_MAKER_ID = "userId";

    // TODO add attribs from enums

    // TODO get from env vars
    protected static final String DB_Name = "ARANGO_DB";
    protected static final String COMMENT_COLLECTION_NAME = "Comment";
    protected static final String CONTENT_COMMENT_COLLECTION_NAME = "ContentComment";
    protected static final String USER_COLLECTION_NAME = "User";
    protected static final String USER_CREATE_COMMENT_COLLECTION_NAME = "UserCreateComment";
    protected static final String USER_LIKE_COMMENT_COLLECTION_NAME = "UserLikeComment";
    protected static final String USER_DISLIKE_COMMENT_COLLECTION_NAME = "UserDislikeComment";


}
