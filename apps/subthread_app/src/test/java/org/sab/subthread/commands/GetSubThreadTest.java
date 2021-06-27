package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.couchbase.Couchbase;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.subthread.SubThreadApp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetSubThreadTest {
    private static final String DB_NAME = System.getenv("ARANGO_DB");
    private static final String threadId = "TestThread", userId = "TestUser";
    private static Arango arango;
    private static BaseDocument thread, user;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getConnectedInstance();
            SubThreadApp.startCouchbaseConnection();

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
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        removeObjectFromCollection(thread, "Thread");
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

    private static JSONObject createSubthread(String creatorId, String parentThreadId, String title, String content, boolean hasImage) {
        JSONObject body = new JSONObject();
        body.put(SubThreadAttributes.PARENT_THREAD_ID.getHTTP(), parentThreadId);
        body.put(SubThreadAttributes.TITLE.getHTTP(), title);
        body.put(SubThreadAttributes.CONTENT.getHTTP(), content);
        body.put(SubThreadAttributes.HAS_IMAGE.getHTTP(), Boolean.toString(hasImage));

        JSONObject uriParams = new JSONObject();
//        uriParams.put(SubThreadAttributes.CREATOR_ID.getHTTP(), creatorId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        CreateSubThread createSubThread = new CreateSubThread();
        return new JSONObject(createSubThread.execute(request));
    }

    private static JSONObject getSubThread(String subThreadId) {
        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadAttributes.SUBTHREAD_ID.getHTTP(), subThreadId);

        JSONObject request = new JSONObject();
        request.put("body", new JSONObject());
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        GetSubThread getSubThread = new GetSubThread();

        return new JSONObject(getSubThread.execute(request));
    }

    @Test
    public void canGetSubThread() {
        final String title = "Epsilon", content = "Rules!";
        final boolean hasImage = false;
        JSONObject createSubThreadResponse = createSubthread(userId, threadId, title, content, hasImage);

        assertEquals(200, createSubThreadResponse.getInt("statusCode"));

        final JSONObject createSubThreadResponseData = createSubThreadResponse.getJSONObject("data");

        final String subThreadId = createSubThreadResponseData.getString(SubThreadAttributes.SUBTHREAD_ID.getDb());

        final JSONObject response = getSubThread(subThreadId);

        assertEquals(200, response.getInt("statusCode"));

        final JSONObject responseData = response.getJSONObject("data");

        assertEquals(responseData.getString(SubThreadAttributes.SUBTHREAD_ID.getDb()), subThreadId);
        assertEquals(responseData.getString(SubThreadAttributes.PARENT_THREAD_ID.getDb()), threadId);
        assertEquals(responseData.getString(SubThreadAttributes.CREATOR_ID.getDb()), userId);
        assertEquals(responseData.getString(SubThreadAttributes.TITLE.getDb()), title);
        assertEquals(responseData.getString(SubThreadAttributes.CONTENT.getDb()), content);
        assertEquals(responseData.getBoolean(SubThreadAttributes.HAS_IMAGE.getDb()), hasImage);
        assertEquals(responseData.getInt(SubThreadAttributes.LIKES.getDb()), 0);
        assertEquals(responseData.getInt(SubThreadAttributes.DISLIKES.getDb()), 0);
    }

    @Test
    public void cannotGetNonExistingSubThread() {
        final String dummySubThreadId = "DummySubThread";

        final JSONObject response = getSubThread(dummySubThreadId);

        assertEquals(404, response.getInt("statusCode"));
        assertEquals(SubThreadCommand.OBJECT_NOT_FOUND, response.getString("msg"));
    }
}