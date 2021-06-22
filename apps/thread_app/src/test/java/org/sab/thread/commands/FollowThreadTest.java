package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.couchbase.Couchbase;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.thread.ThreadApp;

import static org.junit.Assert.*;

public class FollowThreadTest {
    private static final String DB_NAME = System.getenv("ARANGO_DB");
    private static final String threadId = "TestThread", threadOwnerId = "ThreadOwner", followerId = "TestUser";
    private static Arango arango;
    private static BaseDocument thread, threadOwner, follower;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            ThreadApp.startCouchbaseConnection();
            arango.createDatabaseIfNotExists(DB_NAME);

            threadOwner = new BaseDocument();
            threadOwner.setKey(threadOwnerId);
            threadOwner.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
            threadOwner.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
            addObjectToCollection(threadOwner, "User");

            follower = new BaseDocument();
            follower.setKey(followerId);
            follower.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
            follower.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
            addObjectToCollection(follower, "User");

            thread = new BaseDocument();
            thread.setKey(threadId);
            thread.addAttribute(ThreadAttributes.DESCRIPTION.getDb(), "description");
            thread.addAttribute(ThreadAttributes.CREATOR_ID.getDb(), threadOwner);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            thread.addAttribute(ThreadAttributes.DATE_CREATED.getDb(), sqlDate);
            thread.addAttribute(ThreadAttributes.NUM_OF_FOLLOWERS.getDb(), 0);
            addObjectToCollection(thread, "Thread");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        removeObjectFromCollection(thread, "Thread");
        removeObjectFromCollection(threadOwner, "User");
        removeObjectFromCollection(follower, "User");
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

    private static JSONObject userFollowsThread(String userId, String threadName) {
        JSONObject body = new JSONObject();
        body.put(ThreadAttributes.THREAD_NAME.getHTTP(), threadName);

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        FollowThread followThread = new FollowThread();
        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return new JSONObject(followThread.execute(request));
    }

    private static JSONObject userUnfollowsThread(String userId, String threadName) {
        return userFollowsThread(userId, threadName);
    }

    private boolean edgeExistsFromUserToThread(String edgeCollectionName, String userId, String threadId) {
        final String edgeId = arango.getSingleEdgeId(DB_NAME, edgeCollectionName, ThreadCommand.USER_COLLECTION_NAME + "/" + userId, ThreadCommand.THREAD_COLLECTION_NAME + "/" + threadId);

        return !edgeId.equals("");
    }

    @Test
    public void userFollowsThread() {
        BaseDocument threadDocument = arango.readDocument(DB_NAME, ThreadCommand.THREAD_COLLECTION_NAME, threadId);
        final int initialFollowerCount = Integer.parseInt(String.valueOf(threadDocument.getAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB)));

        JSONObject response = userFollowsThread(followerId, threadId);

        assertEquals(200, response.getInt("statusCode"));
        assertEquals(ThreadCommand.FOLLOWED_THREAD_SUCCESSFULLY, ((JSONObject) response.get("data")).getString("msg"));

        threadDocument = arango.readDocument(DB_NAME, ThreadCommand.THREAD_COLLECTION_NAME, threadId);
        final int updatedFollowerCount = Integer.parseInt(String.valueOf(threadDocument.getAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(initialFollowerCount + 1, updatedFollowerCount);

        assertTrue(edgeExistsFromUserToThread(ThreadCommand.USER_FOLLOW_THREAD_COLLECTION_NAME, followerId, threadId));

        userUnfollowsThread(followerId, threadId);
    }

    @Test
    public void userUnfollowsThread() {
        BaseDocument threadDocument = arango.readDocument(DB_NAME, ThreadCommand.THREAD_COLLECTION_NAME, threadId);
        final int initialFollowerCount = Integer.parseInt(String.valueOf(threadDocument.getAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB)));

        JSONObject response = userFollowsThread(followerId, threadId);

        assertEquals(200, response.getInt("statusCode"));

        response = userUnfollowsThread(followerId, threadId);

        assertEquals(200, response.getInt("statusCode"));
        assertEquals(ThreadCommand.UNFOLLOWED_THREAD_SUCCESSFULLY, ((JSONObject) response.get("data")).getString("msg"));

        threadDocument = arango.readDocument(DB_NAME, ThreadCommand.THREAD_COLLECTION_NAME, threadId);
        final int updatedFollowerCount = Integer.parseInt(String.valueOf(threadDocument.getAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(initialFollowerCount, updatedFollowerCount);

        assertFalse(edgeExistsFromUserToThread(ThreadCommand.USER_FOLLOW_THREAD_COLLECTION_NAME, followerId, threadId));
    }

    @Test
    public void userCannotFollowNonExistingThread() {
        final String nonExistingThreadId = "ThreadThatDoesNotExist";

        final JSONObject response = userFollowsThread(followerId, nonExistingThreadId);

        assertEquals(400, response.getInt("statusCode"));
        assertEquals(ThreadCommand.THREAD_DOES_NOT_EXIST, response.get("msg"));

        assertFalse(edgeExistsFromUserToThread(ThreadCommand.USER_FOLLOW_THREAD_COLLECTION_NAME, followerId, threadId));
    }
}