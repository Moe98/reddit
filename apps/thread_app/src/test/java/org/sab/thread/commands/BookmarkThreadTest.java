package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;

import static org.junit.Assert.*;

public class BookmarkThreadTest {
    private static final String DB_NAME = System.getenv("ARANGO_DB");
    private static final String threadId = "TestThread", threadOwnerId = "ThreadOwner", bookmarkingUserId = "TestUser";
    private static Arango arango;
    private static BaseDocument thread, threadOwner, bookmarkingUser;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            arango.createDatabaseIfNotExists(DB_NAME);

            threadOwner = new BaseDocument();
            threadOwner.setKey(threadOwnerId);
            threadOwner.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
            threadOwner.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
            addObjectToCollection(threadOwner, "User");

            bookmarkingUser = new BaseDocument();
            bookmarkingUser.setKey(bookmarkingUserId);
            bookmarkingUser.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
            bookmarkingUser.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
            addObjectToCollection(bookmarkingUser, "User");

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
        removeObjectFromCollection(bookmarkingUser, "User");
        arango.dropDatabase(DB_NAME);
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        arango.createCollectionIfNotExists(DB_NAME, collectionName, false);
        arango.createDocument(DB_NAME, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(DB_NAME, collectionName, document.getKey());
    }

    private static JSONObject userBookmarksThread(String userId, String threadName) {
        JSONObject body = new JSONObject();
        body.put(ThreadAttributes.THREAD_NAME.getHTTP(), threadName);

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        BookmarkThread bookmarkThread = new BookmarkThread();

        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        return new JSONObject(bookmarkThread.execute(request));
    }

    private static JSONObject userUnbookmarksThread(String userId, String threadName) {
        return userBookmarksThread(userId, threadName);
    }

    private boolean edgeExistsFromUserToThread(String edgeCollectionName, String userId, String threadId) {
        final String edgeId = arango.getSingleEdgeId(DB_NAME, edgeCollectionName, ThreadCommand.USER_COLLECTION_NAME + "/" + userId, ThreadCommand.THREAD_COLLECTION_NAME + "/" + threadId);

        return !edgeId.equals("");
    }

    @Test
    public void userBookmarksThread() {
        JSONObject response = userBookmarksThread(bookmarkingUserId, threadId);

        assertEquals(200, response.getInt("statusCode"));
        assertEquals(ThreadCommand.BOOKMARKED_THREAD_SUCCESSFULLY, ((JSONObject) response.get("data")).getString("msg"));

        assertTrue(edgeExistsFromUserToThread(ThreadCommand.USER_BOOKMARK_THREAD_COLLECTION_NAME, bookmarkingUserId, threadId));

        userUnbookmarksThread(bookmarkingUserId, threadId);
    }


    @Test
    public void userUnbookmarksThread() {
        JSONObject response = userBookmarksThread(bookmarkingUserId, threadId);

        assertEquals(200, response.getInt("statusCode"));

        response = userUnbookmarksThread(bookmarkingUserId, threadId);

        assertEquals(200, response.getInt("statusCode"));
        assertEquals(ThreadCommand.UNBOOKMARKED_THREAD_SUCCESSFULLY, ((JSONObject) response.get("data")).getString("msg"));

        assertFalse(edgeExistsFromUserToThread(ThreadCommand.USER_BOOKMARK_THREAD_COLLECTION_NAME, bookmarkingUserId, threadId));
    }

    @Test
    public void userCannotBookmarkNonExistingThread() {
        final String nonExistingThreadId = "ThreadThatDoesNotExist";

        final JSONObject response = userBookmarksThread(bookmarkingUserId, nonExistingThreadId);

        assertEquals(400, response.getInt("statusCode"));
        assertEquals(ThreadCommand.THREAD_DOES_NOT_EXIST, response.get("msg"));

        assertFalse(edgeExistsFromUserToThread(ThreadCommand.USER_BOOKMARK_THREAD_COLLECTION_NAME, bookmarkingUserId, nonExistingThreadId));
    }
}