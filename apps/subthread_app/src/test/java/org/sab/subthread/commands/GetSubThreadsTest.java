package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetSubThreadsTest {
    final private static String moeId = "Moe", mantaId = "Manta";
    final private static String parentThreadId1 = "asmakElRayes7amido", title1 = "gelaty azza is better", content1 = "fish is ya3", hasImage1 = "false";
    final private static String parentThreadId2 = "GelatiAzza", title2 = "fish is better", content2 = "fish is better", hasImage2 = "false";
    private static Arango arango;
    private static BaseDocument thread1, thread2;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();

            arango.createDatabaseIfNotExists(SubThreadCommand.TEST_DB_Name);

            createThreads();
            createOneSubThread(parentThreadId1, title1, content1, hasImage1, moeId);
            create100SubThread(parentThreadId2, title2, content2, hasImage2, mantaId);


        } catch (Exception e) {
            System.out.println("failed");
            fail(e.getMessage());
        }
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        if (!arango.collectionExists(SubThreadCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(SubThreadCommand.TEST_DB_Name, collectionName, false);
        }

        arango.createDocument(SubThreadCommand.TEST_DB_Name, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(SubThreadCommand.TEST_DB_Name, collectionName, document.getKey());
    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(SubThreadCommand.TEST_DB_Name);
    }

    public static void createThreads() {
        thread1 = new BaseDocument();
        thread1.setKey(parentThreadId1);
        thread1.addAttribute(SubThreadCommand.THREAD_CREATOR_ID_DB, mantaId);
        thread1.addAttribute(SubThreadCommand.THREAD_DESCRIPTION_DB, "agmad subreddit fl wogod");
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        thread1.addAttribute(SubThreadCommand.THREAD_DATE_CREATED_DB, sqlDate);
        thread1.addAttribute(SubThreadCommand.THREAD_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread1, SubThreadCommand.THREAD_COLLECTION_NAME);

        thread2 = new BaseDocument();
        thread2.setKey(parentThreadId2);
        thread2.addAttribute(SubThreadCommand.THREAD_CREATOR_ID_DB, moeId);
        thread2.addAttribute(SubThreadCommand.THREAD_DESCRIPTION_DB, "tany agmad subreddit fl wogod");
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        thread2.addAttribute(SubThreadCommand.THREAD_DATE_CREATED_DB, sqlDate2);
        thread2.addAttribute(SubThreadCommand.THREAD_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread2, SubThreadCommand.THREAD_COLLECTION_NAME);

    }

    public static void createOneSubThread(String parentThreadId, String title, String content, String hasImage, String creatorId) {
        CreateSubThread tc = new CreateSubThread();

        JSONObject body = new JSONObject();
        body.put(SubThreadCommand.PARENT_THREAD_ID, parentThreadId);
        body.put(SubThreadCommand.TITLE, title);
        body.put(SubThreadCommand.CONTENT, content);
        body.put(SubThreadCommand.HASIMAGE, hasImage);

        JSONObject uriParams = new JSONObject();
//        uriParams.put("creatorId", creatorId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        tc.execute(request);
    }

    public static void create100SubThread(String parentThreadId, String title, String content, String hasImage, String creatorId) {
        CreateSubThread tc = new CreateSubThread();
        for (int i = 0; i < 100; i++) {
            JSONObject body = new JSONObject();
            body.put(SubThreadCommand.PARENT_THREAD_ID, parentThreadId);
            body.put(SubThreadCommand.TITLE, title);
            body.put(SubThreadCommand.CONTENT, content);
            body.put(SubThreadCommand.HASIMAGE, hasImage);

            JSONObject uriParams = new JSONObject();
//            uriParams.put("creatorId", creatorId);

            JSONObject request = new JSONObject();
            request.put("body", body);
            request.put("methodType", "POST");
            request.put("uriParams", uriParams);

            JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, creatorId);
            AuthParamsHandler.putAuthorizedParams(request, claims);

            tc.execute(request);
        }
    }

    public static String getSubThread(String parentSubthreadId) {
        GetSubThreads getSubthreads = new GetSubThreads();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadCommand.THREAD_ID, parentSubthreadId);
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);
        
        return getSubthreads.execute(request);
    }

    @Test
    public void T01_GetOneSubThread() {
        String response = getSubThread(parentThreadId1);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(1, dataArr.length());
        assertEquals(moeId, ((JSONObject) dataArr.get(0)).getString(SubThreadCommand.CREATOR_ID_DB));
    }

    @Test
    public void T01_Get100SubThread() {
        String response = getSubThread(parentThreadId2);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(100, dataArr.length());
        for (int i = 0; i < 100; i++)
            assertEquals(mantaId, ((JSONObject) dataArr.get(i)).getString(SubThreadCommand.CREATOR_ID_DB));
    }

    @Test
    public void T01_GetSubThreadsOfNonExistentThread() {
        String response = getSubThread("nonExistant");
        JSONObject responseJson = new JSONObject(response);
        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(SubThreadCommand.OBJECT_NOT_FOUND, responseJson.getString("msg"));
    }
}
