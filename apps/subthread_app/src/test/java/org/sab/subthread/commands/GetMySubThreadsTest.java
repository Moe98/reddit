package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class GetMySubThreadsTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String parentThreadId1 = "asmakElRayes7amido", title1 = "gelaty azza is better", content1 = "fish is ya3", hasImage1 = "false";
    final private static String parentThreadId2 = "GelatiAzza", title2 = "fish is better", content2 = "fish is better", hasImage2 = "false";
    final private static String parentThreadId3 = "karateenBaraka", title3 = "water is better", content3 = "water", hasImage3 = "false";
    private static Arango arango;
    private static BaseDocument thread1, thread2, moe, manta, lujine;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            assertTrue(arango.isConnected());
            arango.createDatabaseIfNotExists(SubThreadCommand.TEST_DB_Name);
            createUsers();
            createThreads();
            createSubThread(parentThreadId1,title1,content1,hasImage1,mantaId,50);
            createSubThread(parentThreadId1,title2,content2,hasImage2,moeId,50);
            createSubThread(parentThreadId2,title2,content2,hasImage2,moeId,50);
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
        arango.connectIfNotConnected();
        removeObjectFromCollection(thread1, SubThreadCommand.THREAD_COLLECTION_NAME);
        arango.connectIfNotConnected();
        removeObjectFromCollection(thread2, SubThreadCommand.THREAD_COLLECTION_NAME);
        arango.connectIfNotConnected();
        arango.dropDatabase(SubThreadCommand.TEST_DB_Name);
    }

    public static void createUsers(){
        moe = new BaseDocument();
        moe.setKey(moeId);
        moe.addAttribute(SubThreadCommand.IS_DELETED_DB, false);
        moe.addAttribute(SubThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(moe, SubThreadCommand.USER_COLLECTION_NAME);

        manta = new BaseDocument();
        manta.setKey(mantaId);
        manta.addAttribute(SubThreadCommand.IS_DELETED_DB, false);
        manta.addAttribute(SubThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(manta, SubThreadCommand.USER_COLLECTION_NAME);

        lujine = new BaseDocument();
        lujine.setKey(lujineId);
        lujine.addAttribute(SubThreadCommand.IS_DELETED_DB, false);
        lujine.addAttribute(SubThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(lujine, SubThreadCommand.USER_COLLECTION_NAME);
    }

    public static void createThreads(){
        thread1 = new BaseDocument();
        thread1.setKey(parentThreadId1);
        thread1.addAttribute(SubThreadCommand.THREAD_CREATOR_ID_DB, mantaId);
        thread1.addAttribute(SubThreadCommand.THREAD_DESCRIPTION_DB,  "agmad subreddit fl wogod");
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        thread1.addAttribute(SubThreadCommand.THREAD_DATE_CREATED_DB, sqlDate);
        thread1.addAttribute(SubThreadCommand.THREAD_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread1, SubThreadCommand.THREAD_COLLECTION_NAME);

        thread2 = new BaseDocument();
        thread2.setKey(parentThreadId2);
        thread2.addAttribute(SubThreadCommand.THREAD_CREATOR_ID_DB, moeId);
        thread2.addAttribute(SubThreadCommand.THREAD_DESCRIPTION_DB,  "tany agmad subreddit fl wogod");
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        thread2.addAttribute(SubThreadCommand.THREAD_DATE_CREATED_DB, sqlDate2);
        thread2.addAttribute(SubThreadCommand.THREAD_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread2, SubThreadCommand.THREAD_COLLECTION_NAME);
    }

    public static void createSubThread(String parentThreadId, String title, String content, String hasImage, String creatorId, int amount) {
        CreateSubThread tc = new CreateSubThread();
        for(int i = 0; i < amount; i++) {
            JSONObject body = new JSONObject();
            body.put(SubThreadCommand.PARENT_THREAD_ID, parentThreadId);
            body.put(SubThreadCommand.TITLE, title);
            body.put(SubThreadCommand.CONTENT, content);
            body.put(SubThreadCommand.HASIMAGE, hasImage);

            JSONObject uriParams = new JSONObject();
            uriParams.put("creatorId", creatorId);

            JSONObject request = new JSONObject();
            request.put("body", body);
            request.put("methodType", "POST");
            request.put("uriParams", uriParams);

            tc.execute(request);
        }
    }

    public static String getMySubThreads(String userId){
        GetMySubThreads getMySubthreads = new GetMySubThreads();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadCommand.USER_ID, userId);
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        return getMySubthreads.execute(request);
    }

    @Test
    public void T01_GetMySubThreads() {
        arango.connectIfNotConnected();
        String response = getMySubThreads(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray)(responseJson.get("data"));
        assertEquals(50, dataArr.length());
        for(int i = 0; i<50;i++)
            assertEquals(mantaId, ((JSONObject)dataArr.get(i)).getString(SubThreadCommand.CREATOR_ID_DB));
    }

    @Test
    public void T02_GetMySubThreads() {
        arango.connectIfNotConnected();
        String response = getMySubThreads(moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray)(responseJson.get("data"));
        assertEquals(100, dataArr.length());
        for(int i = 0; i<100;i++)
            assertEquals(moeId, ((JSONObject)dataArr.get(i)).getString(SubThreadCommand.CREATOR_ID_DB));
    }

    @Test
    public void T03_GetMySubThreads() {
        arango.connectIfNotConnected();
        String response = getMySubThreads(lujineId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray)(responseJson.get("data"));
        assertEquals(0, dataArr.length());
    }
}
