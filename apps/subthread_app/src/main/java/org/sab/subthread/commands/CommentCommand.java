package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.*;
import org.sab.models.user.UserAttributes;
import org.sab.service.validation.CommandWithVerification;


public abstract class CommentCommand extends CommandWithVerification {

    // TODO rename |PARENT_SUBTHREAD_ID| to |PARENT_CONTENT_ID|.
    protected static final String PARENT_SUBTHREAD_ID = CommentAttributes.PARENT_SUBTHREAD_ID.getHTTP();
    protected static final String CREATOR_ID = CommentAttributes.CREATOR_ID.getHTTP();
    protected static final String CONTENT = CommentAttributes.CONTENT.getHTTP();
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

    protected static final String OBJECT_NOT_FOUND = "The data you are requested does not exist.";
    protected static final String REQUESTER_NOT_AUTHOR = "You are not the author of this comment";

    // Subthread attributes
    // http
    protected static final String SUBTHREAD_ID = SubThreadAttributes.SUBTHREAD_ID.getHTTP();

    // Subthread attributes
    // db
    protected static final String SUBTHREAD_PARENT_THREAD_ID_DB = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    protected static final String SUBTHREAD_CREATOR_ID_DB = SubThreadAttributes.CREATOR_ID.getDb();

    protected static final String SUBTHREAD_TITLE_DB = SubThreadAttributes.TITLE.getDb();
    protected static final String SUBTHREAD_CONTENT_DB = SubThreadAttributes.CONTENT.getDb();

    protected static final String SUBTHREAD_LIKES_DB = SubThreadAttributes.LIKES.getDb();
    protected static final String SUBTHREAD_DISLIKES_DB = SubThreadAttributes.DISLIKES.getDb();

    protected static final String SUBTHREAD_HAS_IMAGE_DB = SubThreadAttributes.HAS_IMAGE.getDb();
    protected static final String SUBTHREAD_DATE_CREATED_DB = SubThreadAttributes.DATE_CREATED.getDb();


    // User attributes
    protected static final String USERNAME = UserAttributes.USERNAME.toString();

    protected static final String USER_IS_DELETED_DB = UserAttributes.IS_DELETED.getArangoDb();
    protected static final String USER_NUM_OF_FOLLOWERS_DB = UserAttributes.NUM_OF_FOLLOWERS.getArangoDb();

    // Thread attributes
    protected static final String THREAD_DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String THREAD_CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String THREAD_NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String THREAD_DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();
    // TODO add attribs from enums

    // TODO get from env vars
    protected static final String DB_Name = System.getenv("ARANGO_DB");
    protected static final String TEST_DB_Name = DB_Name;
    // TODO bad name
    protected static final String COMMENT_COLLECTION_NAME = CollectionNames.COMMENT.get();
    protected static final String CONTENT_COMMENT_COLLECTION_NAME = CollectionNames.CONTENT_COMMENT.get();
    protected static final String USER_COLLECTION_NAME = CollectionNames.USER.get();
    protected static final String USER_CREATE_COMMENT_COLLECTION_NAME = CollectionNames.USER_CREATE_COMMENT.get();
    protected static final String USER_LIKE_COMMENT_COLLECTION_NAME = CollectionNames.USER_LIKE_COMMENT.get();
    protected static final String USER_DISLIKE_COMMENT_COLLECTION_NAME = CollectionNames.USER_DISLIKE_COMMENT.get();
    protected static final String SUBTHREAD_COLLECTION_NAME = CollectionNames.SUBTHREAD.get();
    protected static final String THREAD_COLLECTION_NAME = CollectionNames.THREAD.get();
    protected static final String USER_CREATE_SUBTHREAD_COLLECTION_NAME = CollectionNames.USER_CREATE_SUBTHREAD.get();

    // TODO get queueName from somewhere instead of hardcoding it
    protected static final String Notification_Queue_Name = "NOTIFICATION_REQ";
    // TODO get function name from somewhere consitant
    protected static final String SEND_NOTIFICATION_FUNCTION_NAME = "SEND_NOTIFICATION";

    protected final JSONObject baseDocumentToJson(BaseDocument document) {
        final String commentId = document.getKey();
        final String parentSubThreadId = (String) document.getAttribute(PARENT_SUBTHREAD_ID_DB);
        final String creatorId = (String) document.getAttribute(CREATOR_ID_DB);
        final String content = (String) document.getAttribute(CONTENT_DB);
        final String parentContentType = (String) document.getAttribute(PARENT_CONTENT_TYPE_DB);
        final int likes = Integer.parseInt(String.valueOf(document.getAttribute(LIKES_DB)));
        final int dislikes = Integer.parseInt(String.valueOf(document.getAttribute(DISLIKES_DB)));
        final String dateCreated = (String) document.getAttribute(DATE_CREATED_DB);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setParentId(parentSubThreadId);
        comment.setCreatorId(creatorId);
        comment.setContent(content);
        comment.setParentContentType(parentContentType);
        comment.setLikes(likes);
        comment.setDislikes(dislikes);
        comment.setDateCreated(dateCreated);

        return comment.toJSON();
    }

    protected final boolean subthreadExistsInCouchbase(String key) {
        return Couchbase.getInstance().documentExists(CouchbaseBuckets.RECOMMENDED_SUB_THREADS.get(), key);
    }

    protected final boolean commentExistsInCouchbase(String key) {
        return Couchbase.getInstance().documentExists(CouchbaseBuckets.COMMENTS.get(), key);
    }

    protected final boolean existsInArango(String collectionName, String key) {
        return Arango.getInstance().documentExists(DB_Name, collectionName, key);
    }

    protected final void deleteDocumentFromCouchbase(String bucketName, String key) {
        Couchbase.getInstance().deleteDocumentIfExists(bucketName, key);
    }

    protected final BaseDocument getDocumentFromCouchbase(String bucketName, String key) {
        JSONObject comment = Couchbase.getInstance().getDocumentJson(bucketName, key);

        BaseDocument myObject = new BaseDocument();

        myObject.addAttribute(PARENT_SUBTHREAD_ID_DB, comment.get(PARENT_SUBTHREAD_ID_DB));
        myObject.addAttribute(CREATOR_ID_DB, comment.get(CREATOR_ID_DB));
        myObject.addAttribute(CONTENT_DB, comment.get(CONTENT_DB));
        myObject.addAttribute(PARENT_CONTENT_TYPE_DB, comment.get(PARENT_CONTENT_TYPE_DB));
        myObject.addAttribute(LIKES_DB, comment.get(LIKES_DB));
        myObject.addAttribute(DISLIKES_DB, comment.get(DISLIKES_DB));
        myObject.addAttribute(DATE_CREATED_DB, comment.get(DATE_CREATED_DB));
        return myObject;
    }

    protected final void replaceDocumentInCouchbase(String bucketName, String key, BaseDocument document) {
        Couchbase.getInstance().replaceDocument(bucketName, key, baseDocumentToJson(document));
    }

    protected final void upsertDocumentInCouchbase(String bucketName, String key, BaseDocument document) {
        Couchbase.getInstance().upsertDocument(bucketName, key, baseDocumentToJson(document));
    }

}
