package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.couchbase.Couchbase;
import org.sab.models.CommentAttributes;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.subthread.SubThreadApp;

import static org.junit.Assert.*;

public class GetCommentTest {
    private static final String DB_NAME = System.getenv("ARANGO_DB");
    private static final String threadId = "TestThread", subThreadId = "TestSubThread", userId = "TestUser";
    private static Arango arango;
    private static BaseDocument thread, subThread, user;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            SubThreadApp.startCouchbaseConnection();

            // TODO: Use a test DB if possible.
            arango.createDatabaseIfNotExists(DB_NAME);

            user = new BaseDocument();
            user.setKey(userId);
            user.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
            user.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
            addObjectToCollection(user, "User");

            thread = new BaseDocument();
            thread.setKey(threadId);
            thread.addAttribute(ThreadAttributes.DESCRIPTION.getDb(), "description");
            thread.addAttribute(ThreadAttributes.CREATOR_ID.getDb(), userId);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            thread.addAttribute(ThreadAttributes.DATE_CREATED.getDb(), sqlDate);
            thread.addAttribute(ThreadAttributes.NUM_OF_FOLLOWERS.getDb(), 0);
            addObjectToCollection(thread, "Thread");

            subThread = new BaseDocument();
            subThread.setKey(subThreadId);
            subThread.addAttribute(SubThreadAttributes.PARENT_THREAD_ID.getDb(), threadId);
            subThread.addAttribute(SubThreadAttributes.CREATOR_ID.getDb(), userId);
            subThread.addAttribute(SubThreadAttributes.TITLE.getDb(), "title");
            subThread.addAttribute(SubThreadAttributes.CONTENT.getDb(), "content");
            subThread.addAttribute(SubThreadAttributes.LIKES.getDb(), 0);
            subThread.addAttribute(SubThreadAttributes.DISLIKES.getDb(), 0);
            subThread.addAttribute(SubThreadAttributes.HAS_IMAGE.getDb(), false);
            sqlDate = new java.sql.Date(System.currentTimeMillis());
            subThread.addAttribute(SubThreadAttributes.DATE_CREATED.getDb(), sqlDate);
            addObjectToCollection(subThread, "Subthread");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        removeObjectFromCollection(thread, "Thread");
        removeObjectFromCollection(subThread, "Subthread");
        removeObjectFromCollection(user, "User");
        arango.dropDatabase(DB_NAME);
        Couchbase.getInstance().disconnect();
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        arango.createCollectionIfNotExists(DB_NAME, collectionName, false);
        arango.createDocument(DB_NAME, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(DB_NAME, collectionName, document.getKey());
    }

    private static JSONObject createComment(String parentId, String content, String parentContentType, String actionMakerId) {
        JSONObject body = new JSONObject();
        body.put(CommentAttributes.PARENT_SUBTHREAD_ID.getHTTP(), parentId);
        body.put(CommentAttributes.CONTENT.getHTTP(), content);
        body.put(CommentAttributes.PARENT_CONTENT_TYPE.getHTTP(), parentContentType);

        JSONObject uriParams = new JSONObject();
//        uriParams.put(CommentAttributes.ACTION_MAKER_ID.getHTTP(), actionMakerId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, actionMakerId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        CreateComment createComment = new CreateComment();

        return new JSONObject(createComment.execute(request));
    }

    private static JSONObject getComment(String commentId) {
        JSONObject uriParams = new JSONObject();
        uriParams.put(CommentAttributes.COMMENT_ID.getHTTP(), commentId);

        JSONObject request = new JSONObject();
        request.put("body", new JSONObject());
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        GetComment getComment = new GetComment();

        return new JSONObject(getComment.execute(request));
    }

    @Test
    public void canGetComment() {
        final String content = "Test comment.";
        final JSONObject createCommentResponse = createComment(subThreadId, content, "Subthread", userId);

        assertEquals(200, createCommentResponse.getInt("statusCode"));

        final JSONObject createCommentResponseData = createCommentResponse.getJSONObject("data");

        final String commentId = createCommentResponseData.getString(CommentAttributes.COMMENT_ID.getDb());

        final JSONObject response = getComment(commentId);

        assertEquals(200, response.getInt("statusCode"));

        final JSONObject responseData = response.getJSONObject("data");

        assertEquals(responseData.getString(CommentAttributes.COMMENT_ID.getDb()), commentId);
        assertEquals(responseData.getString(CommentAttributes.PARENT_CONTENT_TYPE.getDb()), "Subthread");
        assertEquals(responseData.getString(CommentAttributes.PARENT_SUBTHREAD_ID.getDb()), subThreadId);
        assertEquals(responseData.getString(CommentAttributes.CREATOR_ID.getDb()), userId);
        assertEquals(responseData.getString(CommentAttributes.CONTENT.getDb()), content);
        assertEquals(responseData.getInt(CommentAttributes.LIKES.getDb()), 0);
        assertEquals(responseData.getInt(CommentAttributes.DISLIKES.getDb()), 0);
    }

    @Test
    public void cannotGetNonExistingComment() {
        final String dummyCommentId = "DummyComment";

        final JSONObject response = getComment(dummyCommentId);

        assertEquals(404, response.getInt("statusCode"));
        assertEquals(CommentCommand.OBJECT_NOT_FOUND, response.getString("msg"));
    }
}