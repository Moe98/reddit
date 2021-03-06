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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetThreadTest {
    private static final String DB_NAME = System.getenv("ARANGO_DB");
    private static final String userId = "TestUser";
    private static Arango arango;
    private static BaseDocument user;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getConnectedInstance();
            ThreadApp.startCouchbaseConnection();
            arango.createDatabaseIfNotExists(DB_NAME);

            user = new BaseDocument();
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

    // TODO should be direct insert
    private static JSONObject createThread(String threadName, String creatorId, String description) {
        JSONObject request = new JSONObject();

        JSONObject body = new JSONObject();
        body.put(ThreadAttributes.THREAD_NAME.getHTTP(), threadName);
        body.put(ThreadAttributes.DESCRIPTION.getHTTP(), description);

        JSONObject uriParams = new JSONObject();
//        uriParams.put(ThreadAttributes.CREATOR_ID.getHTTP(), creatorId);

        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", "POST");

        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        CreateThread createThread = new CreateThread();
        return new JSONObject(createThread.execute(request));
    }

    private static JSONObject getThread(String threadId) {
        JSONObject uriParams = new JSONObject();
        uriParams.put(ThreadAttributes.THREAD_NAME.getHTTP(), threadId);

        JSONObject request = new JSONObject();
        request.put("body", new JSONObject());
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        GetThread getThread = new GetThread();

        return new JSONObject(getThread.execute(request));
    }

    @Test
    public void canGetThread() {
        final String threadName = "Epsilon", description = "Rules!";
        final JSONObject createThreadResponse = createThread(threadName, userId, description);

        assertEquals(200, createThreadResponse.getInt("statusCode"));

        final JSONObject response = getThread(threadName);

        assertEquals(200, response.getInt("statusCode"));

        final JSONObject responseData = response.getJSONObject("data");

        assertEquals(responseData.getString(ThreadAttributes.THREAD_NAME.getDb()), threadName);
        assertEquals(responseData.getString(ThreadAttributes.CREATOR_ID.getDb()), userId);
        assertEquals(responseData.getString(ThreadAttributes.DESCRIPTION.getDb()), description);
        assertEquals(responseData.getInt(ThreadAttributes.NUM_OF_FOLLOWERS.getDb()), 0);
    }

    @Test
    public void cannotGetNonExistingThread() {
        final String dummyThreadId = "DummyThread";

        final JSONObject response = getThread(dummyThreadId);

        assertEquals(404, response.getInt("statusCode"));
        assertEquals(ThreadCommand.OBJECT_NOT_FOUND, response.getString("msg"));
    }
}