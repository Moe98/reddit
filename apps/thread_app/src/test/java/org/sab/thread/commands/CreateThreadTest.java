package org.sab.thread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CreateThreadTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            assertTrue(arango.isConnected());

            arango.createDatabaseIfNotExists(ThreadCommand.TEST_DB_Name);

            createUsers();

        } catch (Exception e) {
            System.out.println("failed");
            fail(e.getMessage());
        }
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        if (!arango.collectionExists(ThreadCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(ThreadCommand.TEST_DB_Name, collectionName, false);
        }

        arango.createDocument(ThreadCommand.TEST_DB_Name, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(ThreadCommand.TEST_DB_Name, collectionName, document.getKey());
    }

    @AfterClass
    public static void tearDown() {
        arango.connectIfNotConnected();
        arango.dropDatabase(ThreadCommand.TEST_DB_Name);
    }

    public static void createUsers() {
        moe = new BaseDocument();
        moe.setKey(moeId);
        moe.addAttribute(ThreadCommand.IS_DELETED_DB, false);
        moe.addAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(moe, ThreadCommand.USER_COLLECTION_NAME);

        manta = new BaseDocument();
        manta.setKey(mantaId);
        manta.addAttribute(ThreadCommand.IS_DELETED_DB, false);
        manta.addAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(manta, ThreadCommand.USER_COLLECTION_NAME);

        lujine = new BaseDocument();
        lujine.setKey(lujineId);
        lujine.addAttribute(ThreadCommand.IS_DELETED_DB, false);
        lujine.addAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(lujine, ThreadCommand.USER_COLLECTION_NAME);
    }

    public static String createThread(String creatorId, String threadName, String description) {
        CreateThread tc = new CreateThread();
        JSONObject request = new JSONObject();

        JSONObject body = new JSONObject();
        body.put(ThreadCommand.THREAD_NAME, threadName);

        body.put(ThreadCommand.DESCRIPTION, description);

        JSONObject uriParams = new JSONObject();
        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", "POST");

        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        return tc.execute(request);
    }

    public static String createThreadWithoutNameAndDesc(String creatorId) {
        CreateThread tc = new CreateThread();
        JSONObject request = new JSONObject();
        JSONObject body = new JSONObject();
        JSONObject uriParams = new JSONObject();
        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", "POST");
        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return tc.execute(request);
    }

    public static String createThreadWithoutName(String creatorId, String description) {
        CreateThread tc = new CreateThread();
        JSONObject request = new JSONObject();
        JSONObject body = new JSONObject();
        body.put(ThreadCommand.DESCRIPTION, description);
        JSONObject uriParams = new JSONObject();
        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", "POST");
        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return tc.execute(request);
    }

    public static String createThreadWithoutDesc(String creatorId, String name) {
        CreateThread tc = new CreateThread();
        JSONObject request = new JSONObject();
        JSONObject body = new JSONObject();
        body.put(ThreadCommand.THREAD_NAME, name);
        JSONObject uriParams = new JSONObject();
        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", "POST");
        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return tc.execute(request);
    }

    @Test
    public void T01_UserCreateThread() {
        String threadName = "asmakIbn7amedo";
        String description = "agmad thread fl wogoog";
        arango.connectIfNotConnected();
        String response = createThread(mantaId, threadName, description);
        JSONObject responseJson = new JSONObject(response);

        // checking the response of the command
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONObject data = (JSONObject) (responseJson.get("data"));
        assertEquals(description, data.get(ThreadCommand.DESCRIPTION));
        assertEquals(0, data.get(ThreadCommand.NUM_OF_FOLLOWERS));
        assertEquals(mantaId, data.get(ThreadCommand.CREATOR_ID));
        assertEquals(threadName, data.get(ThreadCommand.THREAD_NAME));

        // checking the thread created in DB
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor = arango.filterCollection(ThreadCommand.DB_Name, ThreadCommand.THREAD_COLLECTION_NAME, ThreadCommand.CREATOR_ID_DB, mantaId);
        ArrayList<String> threadAtt = new ArrayList<>();
        threadAtt.add(ThreadCommand.NUM_OF_FOLLOWERS_DB);
        threadAtt.add(ThreadCommand.DESCRIPTION_DB);
        threadAtt.add(ThreadCommand.CREATOR_ID_DB);
        threadAtt.add(ThreadCommand.DATE_CREATED_DB);
        JSONArray threadArr = arango.parseOutput(cursor, ThreadCommand.THREAD_NAME, threadAtt);
        assertEquals(1, threadArr.length());
        assertEquals(description, ((JSONObject) threadArr.get(0)).get(ThreadCommand.DESCRIPTION_DB));
        assertEquals(0, ((JSONObject) threadArr.get(0)).get(ThreadCommand.NUM_OF_FOLLOWERS_DB));
        assertEquals(mantaId, ((JSONObject) threadArr.get(0)).get(ThreadCommand.CREATOR_ID_DB));
        assertEquals(threadName, ((JSONObject) threadArr.get(0)).get(ThreadCommand.THREAD_NAME));

        // checking the creator is a mod
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor2 = arango.filterEdgeCollection(ThreadCommand.DB_Name, ThreadCommand.USER_MOD_THREAD_COLLECTION_NAME, ThreadCommand.USER_COLLECTION_NAME + "/" + mantaId);
        JSONArray threadArr2 = arango.parseOutput(cursor2, ThreadCommand.THREAD_NAME, threadAtt);
        assertEquals(1, threadArr2.length());
        assertEquals(description, ((JSONObject) threadArr2.get(0)).get(ThreadCommand.DESCRIPTION_DB));
        assertEquals(0, ((JSONObject) threadArr2.get(0)).get(ThreadCommand.NUM_OF_FOLLOWERS_DB));
        assertEquals(mantaId, ((JSONObject) threadArr2.get(0)).get(ThreadCommand.CREATOR_ID_DB));
        assertEquals(threadName, ((JSONObject) threadArr2.get(0)).get(ThreadCommand.THREAD_NAME));
    }

    @Test
    public void T02_UserCreateThreadWithoutNameAndDesc() {
        arango.connectIfNotConnected();
        String response = createThreadWithoutNameAndDesc(mantaId);
        JSONObject responseJson = new JSONObject(response);
        assertEquals(400, responseJson.getInt("statusCode"));
        assertEquals("Some attributes were missing: name, description", responseJson.get("msg"));
    }

    @Test
    public void T02_UserCreateThreadWithoutName() {
        String description = "description";
        arango.connectIfNotConnected();
        String response = createThreadWithoutName(mantaId, description);
        JSONObject responseJson = new JSONObject(response);
        assertEquals(400, responseJson.getInt("statusCode"));
        assertEquals("Some attributes were missing: name", responseJson.get("msg"));
    }

    @Test
    public void T02_UserCreateThreadWithoutDesc() {
        String name = "name";
        arango.connectIfNotConnected();
        String response = createThreadWithoutDesc(mantaId, name);
        JSONObject responseJson = new JSONObject(response);
        assertEquals(400, responseJson.getInt("statusCode"));
        assertEquals("Some attributes were missing: description", responseJson.get("msg"));
    }
}
