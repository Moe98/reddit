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
import org.sab.service.validation.HTTPMethod;
import org.sab.thread.ThreadApp;

import static org.junit.Assert.*;

public class UpdateThreadTest {
    private static final String DB_NAME = System.getenv("ARANGO_DB");
    private static final String userId = "TestUser";
    private static Arango arango;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getConnectedInstance();
            ThreadApp.startCouchbaseConnection();
            // TODO: Use a test DB if possible.
            arango.createDatabaseIfNotExists(DB_NAME);

            final BaseDocument user = new BaseDocument();
            user.setKey(userId);
            user.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
            user.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
            addObjectToCollection(user, "User");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(DB_NAME);
        Couchbase.getInstance().disconnect();
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        arango.createCollectionIfNotExists(DB_NAME, collectionName, false);
        arango.createDocument(DB_NAME, collectionName, document);
    }


    private static JSONObject createThread(String description, String creatorId, String threadName) {
        JSONObject request = new JSONObject();

        JSONObject body = new JSONObject();
        body.put(ThreadAttributes.THREAD_NAME.getHTTP(), threadName);
        body.put(ThreadAttributes.DESCRIPTION.getHTTP(), description);

        JSONObject uriParams = new JSONObject();

        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", "POST");

        CreateThread createThread = new CreateThread();
        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return new JSONObject(createThread.execute(request));
    }

    private static JSONObject updateThread(String description, String actionMakerId, String threadName) {
        JSONObject body = new JSONObject();
        body.put(ThreadAttributes.DESCRIPTION.getHTTP(), description);

        JSONObject uriParams = new JSONObject();
        uriParams.put(ThreadAttributes.THREAD_NAME.getHTTP(), threadName);

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.PUT);

        UpdateThread updateThread = new UpdateThread();
        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, actionMakerId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return new JSONObject(updateThread.execute(request));
    }

    @Test
    public void userUpdatesThread() {
        final String threadName = "Epsilon";
        final String description = "Description.";
        final JSONObject initialThreadResponse = createThread(description, userId, threadName);

        assertEquals(200, initialThreadResponse.getInt("statusCode"));

        final String updatedDescription = "Updated Description.";

        final JSONObject updatedThreadResponse = updateThread(updatedDescription, userId, threadName);

        assertEquals(200, updatedThreadResponse.getInt("statusCode"));

        final JSONObject updatedThreadResponseData = updatedThreadResponse.getJSONObject("data");

        assertEquals(updatedThreadResponseData.getString(ThreadAttributes.THREAD_NAME.getDb()), threadName);
        assertEquals(updatedThreadResponseData.getString(ThreadAttributes.CREATOR_ID.getDb()), userId);
        assertEquals(updatedThreadResponseData.getString(ThreadAttributes.DESCRIPTION.getDb()), updatedDescription);
        assertEquals(updatedThreadResponseData.getInt(ThreadAttributes.NUM_OF_FOLLOWERS.getDb()), 0);
    }

    @Test
    public void userCannotUpdateOthersThreads() {
        final String threadName = "Rules!";
        final String description = "Description.";
        final JSONObject initialThreadResponse = createThread(description, userId, threadName);

        assertEquals(200, initialThreadResponse.getInt("statusCode"));

        final String updatedDescription = "Updated Description.";

        final String dummyUserId = "DummyUser";

        final JSONObject response = updateThread(updatedDescription, dummyUserId, threadName);

        assertEquals(403, response.getInt("statusCode"));
        assertEquals(ThreadCommand.REQUESTER_NOT_AUTHOR, response.getString("msg"));
    }

    @Test
    public void userCannotUpdateNonExistingThread() {
        final String dummyThreadId = "DummyThread";
        final String updatedDescription = "Updated Description.";

        final JSONObject response = updateThread(updatedDescription, userId, dummyThreadId);

        assertEquals(404, response.getInt("statusCode"));
        assertEquals(ThreadCommand.OBJECT_NOT_FOUND, response.getString("msg"));
    }
}