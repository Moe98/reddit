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
import org.sab.service.validation.HTTPMethod;
import org.sab.subthread.SubThreadApp;

import static org.junit.Assert.*;

public class UpdateCommentTest {
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

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.POST);

        CreateComment createComment = new CreateComment();

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, actionMakerId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        return new JSONObject(createComment.execute(request));
    }

    private static JSONObject updateComment(String content, String actionMakerId, String commentId) {
        JSONObject body = new JSONObject();
        body.put(CommentAttributes.CONTENT.getHTTP(), content);

        JSONObject uriParams = new JSONObject();
        uriParams.put(CommentAttributes.COMMENT_ID.getHTTP(), commentId);

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.PUT);

        UpdateComment updateComment = new UpdateComment();

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, actionMakerId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        return new JSONObject(updateComment.execute(request));
    }


    @Test
    public void userUpdatesComment() {
        final String content = "Content.";
        final JSONObject initialCommentResponse = createComment(subThreadId, content, "Subthread", userId);

        assertEquals(200, initialCommentResponse.getInt("statusCode"));

        final JSONObject initialCommentResponseData = initialCommentResponse.getJSONObject("data");

        final String commentId = initialCommentResponseData.getString(CommentAttributes.COMMENT_ID.getDb());
        final String updatedContent = "Updated Content.";

        final JSONObject updatedCommentResponse = updateComment(updatedContent, userId, commentId);

        assertEquals(200, updatedCommentResponse.getInt("statusCode"));

        final JSONObject updatedCommentResponseData = updatedCommentResponse.getJSONObject("data");

        assertEquals(updatedCommentResponseData.getString(CommentAttributes.PARENT_CONTENT_TYPE.getDb()), "Subthread");
        assertEquals(updatedCommentResponseData.getString(CommentAttributes.PARENT_SUBTHREAD_ID.getDb()), subThreadId);
        assertEquals(updatedCommentResponseData.getString(CommentAttributes.CREATOR_ID.getDb()), userId);
        assertEquals(updatedCommentResponseData.getString(CommentAttributes.CONTENT.getDb()), updatedContent);
        assertEquals(updatedCommentResponseData.getInt(CommentAttributes.LIKES.getDb()), 0);
        assertEquals(updatedCommentResponseData.getInt(CommentAttributes.DISLIKES.getDb()), 0);
    }

    @Test
    public void userCannotUpdateOthersComments() {
        final String content = "Content.";
        final JSONObject initialCommentResponse = createComment(subThreadId, content, "Subthread", userId);

        assertEquals(200, initialCommentResponse.getInt("statusCode"));

        final JSONObject initialCommentResponseData = initialCommentResponse.getJSONObject("data");

        final String commentId = initialCommentResponseData.getString(CommentAttributes.COMMENT_ID.getDb());

        final String dummyUserId = "DummyUser";

        final JSONObject response = updateComment(content, dummyUserId, commentId);

        assertEquals(403, response.getInt("statusCode"));
        assertEquals(CommentCommand.REQUESTER_NOT_AUTHOR, response.getString("msg"));
    }

    @Test
    public void userCannotUpdateNonExistingComment() {
        final String dummyCommentId = "DummyComment";
        final String updatedContent = "Updated Content.";

        final JSONObject response = updateComment(updatedContent, userId, dummyCommentId);

        assertEquals(404, response.getInt("statusCode"));
        assertEquals(CommentCommand.OBJECT_NOT_FOUND, response.getString("msg"));
    }
}