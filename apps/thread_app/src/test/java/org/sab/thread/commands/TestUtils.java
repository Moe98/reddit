package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.CommentAttributes;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;

public class TestUtils {

    // user attributes
    final static String USR_IS_DELETED = UserAttributes.IS_DELETED.getDb();
    final static String USR_NUM_OF_FOLLOWERS = UserAttributes.NUM_OF_FOLLOWERS.getDb();

    // thread attribs
    protected static final String THREAD_DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String THREAD_CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String THREAD_NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String THREAD_DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();

    // subthread attribs
    protected static final String SUBTHREAD_PARENT_THREAD_ID_DB = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    protected static final String SUBTHREAD_CREATOR_ID_DB = SubThreadAttributes.CREATOR_ID.getDb();
    protected static final String SUBTHREAD_TITLE_DB = SubThreadAttributes.TITLE.getDb();
    protected static final String SUBTHREAD_CONTENT_DB = SubThreadAttributes.CONTENT.getDb();
    protected static final String SUBTHREAD_LIKES_DB = SubThreadAttributes.LIKES.getDb();
    protected static final String SUBTHREAD_DISLIKES_DB = SubThreadAttributes.DISLIKES.getDb();
    protected static final String SUBTHREAD_HAS_IMAGE_DB = SubThreadAttributes.HAS_IMAGE.getDb();
    protected static final String SUBTHREAD_DATE_CREATED_DB = SubThreadAttributes.DATE_CREATED.getDb();

    // comment attributes

    protected static final String COMM_CREATOR_ID = CommentAttributes.CREATOR_ID.getDb();
    // TODO parent could be a comment...
    protected static final String COMM_PARENT_SUBTHREAD_ID = CommentAttributes.PARENT_SUBTHREAD_ID.getDb();
    protected static final String COMM_PARENT_CONTENT_TYPE = CommentAttributes.PARENT_CONTENT_TYPE.getDb();
    protected static final String COMM_CONTENT = CommentAttributes.CONTENT.getDb();
    protected static final String COMM_DATE_CREATED = CommentAttributes.DATE_CREATED.getDb();
    protected static final String COMM_LIKES = CommentAttributes.LIKES.getDb();
    protected static final String COMM_DISLIKES = CommentAttributes.DISLIKES.getDb();

    protected static BaseDocument addObjectToCollection(Arango arango, BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        final BaseDocument res = arango.createDocument(ThreadCommand.DB_Name, collectionName, document);
        return res;
    }

    protected static BaseDocument setUpUser(String id, boolean isDeleted, int numFollowers) {
        BaseDocument baseDocument = new BaseDocument();
        baseDocument.setKey(id);
        baseDocument.addAttribute(USR_IS_DELETED, isDeleted);
        baseDocument.addAttribute(USR_NUM_OF_FOLLOWERS, numFollowers);
        return baseDocument;
    }

    protected static BaseDocument setUpThread(String threadName, String creatorId, int numFollowers, String description) {
        BaseDocument baseDocument  = new BaseDocument();

        baseDocument.setKey(threadName);
        baseDocument.addAttribute(THREAD_CREATOR_ID_DB, creatorId);
        baseDocument.addAttribute(THREAD_NUM_OF_FOLLOWERS_DB, numFollowers);
        baseDocument.addAttribute(THREAD_DESCRIPTION_DB, description);
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        baseDocument.addAttribute(THREAD_DATE_CREATED_DB, sqlDate);

        return baseDocument;
    }

    protected static BaseDocument setUpSubThreadNoImage(String subthreadId, String parentThreadId, String  creatorId, String title, String content,
                                                        int likes, int dislikes) {
        BaseDocument baseDocument  = new BaseDocument();

        // TODO remove id
        baseDocument.setKey(subthreadId);
        baseDocument.addAttribute(SUBTHREAD_PARENT_THREAD_ID_DB, parentThreadId);
        baseDocument.addAttribute(SUBTHREAD_CREATOR_ID_DB, creatorId);
        baseDocument.addAttribute(SUBTHREAD_TITLE_DB, title);
        baseDocument.addAttribute(SUBTHREAD_CONTENT_DB, content);
        baseDocument.addAttribute(SUBTHREAD_LIKES_DB, likes);
        baseDocument.addAttribute(SUBTHREAD_DISLIKES_DB, dislikes);
        baseDocument.addAttribute(SUBTHREAD_HAS_IMAGE_DB, false);
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        baseDocument.addAttribute(SUBTHREAD_DATE_CREATED_DB, sqlDate);

        return baseDocument;
    }

    protected static BaseDocument setUpComment(String creatorId, String parentId, String parentType,
                                               String content, int likes, int dislikes) {

        BaseDocument baseDocument  = new BaseDocument();
        baseDocument.addAttribute(COMM_CREATOR_ID, creatorId);
        baseDocument.addAttribute(COMM_PARENT_SUBTHREAD_ID, parentId);
        baseDocument.addAttribute(COMM_PARENT_CONTENT_TYPE, parentType);
        baseDocument.addAttribute(COMM_CONTENT, content);
        baseDocument.addAttribute(COMM_LIKES, likes);
        baseDocument.addAttribute(COMM_DISLIKES, dislikes);
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        baseDocument.addAttribute(COMM_DATE_CREATED, sqlDate);

        return baseDocument;
    }

    public static JSONObject makePutRequest(JSONObject body, JSONObject uriParams) {
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);
        return request;
    }
}
