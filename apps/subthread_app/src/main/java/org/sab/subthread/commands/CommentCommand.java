package org.sab.subthread.commands;

import org.sab.models.CommentAttributes;
import org.sab.models.SubThreadAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.service.validation.CommandWithVerification;


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

    protected static final String USER_ID = UserAttributes.USER_ID.getHTTP();
    protected static final String ACTION_MAKER_ID = "userId";

    protected static final String OBJECT_NOT_FOUND = "The data you are requested does not exist.";
    protected static final String REQUESTER_NOT_AUTHOR = "You are not the author of this comment";

    // Subthread attributes
    // http
    protected static final String THREAD_SUBTHREAD_ID = SubThreadAttributes.SUBTHREAD_ID.getHTTP();
    protected static final String THREAD_PARENT_THREAD_ID = SubThreadAttributes.PARENT_THREAD_ID.getHTTP();
    protected static final String THREAD_CREATOR_ID = SubThreadAttributes.CREATOR_ID.getHTTP();

    protected static final String THREAD_TITLE = SubThreadAttributes.TITLE.getHTTP();
    protected static final String THREAD_CONTENT = SubThreadAttributes.CONTENT.getHTTP();

    protected static final String THREAD_LIKES = SubThreadAttributes.LIKES.getHTTP();
    protected static final String THREAD_DISLIKES = SubThreadAttributes.DISLIKES.getHTTP();

    protected static final String THREAD_HASIMAGE = SubThreadAttributes.HAS_IMAGE.getHTTP();

    // Subthread attributes
    // db
    protected static final String THREAD_SUBTHREAD_ID_DB = SubThreadAttributes.SUBTHREAD_ID.getDb();
    protected static final String THREAD_PARENT_THREAD_ID_DB = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    protected static final String THREAD_CREATOR_ID_DB = SubThreadAttributes.CREATOR_ID.getDb();

    protected static final String THREAD_TITLE_DB = SubThreadAttributes.TITLE.getDb();
    protected static final String THREAD_CONTENT_DB = SubThreadAttributes.CONTENT.getDb();

    protected static final String THREAD_LIKES_DB = SubThreadAttributes.LIKES.getDb();
    protected static final String THREAD_DISLIKES_DB = SubThreadAttributes.DISLIKES.getDb();

    protected static final String THREAD_HASIMAGE_DB = SubThreadAttributes.HAS_IMAGE.getDb();
    protected static final String THREAD_DATE_CREATED_DB = SubThreadAttributes.DATE_CREATED.getDb();


    // TODO add attribs from enums

    // TODO get from env vars
    protected static final String DB_Name = System.getenv("ARANGO_DB");
    protected static final String TEST_DB_Name = DB_Name;
    protected static final String COMMENT_COLLECTION_NAME = "Comment";
    protected static final String CONTENT_COMMENT_COLLECTION_NAME = "ContentComment";
    protected static final String USER_COLLECTION_NAME = "User";
    protected static final String USER_CREATE_COMMENT_COLLECTION_NAME = "UserCreateComment";
    protected static final String USER_LIKE_COMMENT_COLLECTION_NAME = "UserLikeComment";
    protected static final String USER_DISLIKE_COMMENT_COLLECTION_NAME = "UserDislikeComment";
    protected static final String SUBTHREAD_COLLECTION_NAME = "Subthread";
}
