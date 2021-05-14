package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetFollowedThreadsTest {
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
            arango.createDatabaseIfNotExists(ThreadCommand.TEST_DB_Name);

            createUsers();
            createThreads();
            followThread(parentThreadId1, mantaId);
            followThread(parentThreadId2, mantaId);
            followThread(parentThreadId3, lujineId);

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

    private static void addObjectToEdgeCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        if (!arango.collectionExists(ThreadCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(ThreadCommand.TEST_DB_Name, collectionName, true);
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

    public static void createThreads() {
        thread1 = new BaseDocument();
        thread1.setKey(parentThreadId1);
        thread1.addAttribute(ThreadCommand.CREATOR_ID_DB, lujineId);
        thread1.addAttribute(ThreadCommand.DESCRIPTION_DB, "agmad subreddit fl wogod");
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        thread1.addAttribute(ThreadCommand.DATE_CREATED_DB, sqlDate);
        thread1.addAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread1, ThreadCommand.THREAD_COLLECTION_NAME);

        thread2 = new BaseDocument();
        thread2.setKey(parentThreadId2);
        thread2.addAttribute(ThreadCommand.CREATOR_ID_DB, lujineId);
        thread2.addAttribute(ThreadCommand.DESCRIPTION_DB, "tany agmad subreddit fl wogod");
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        thread2.addAttribute(ThreadCommand.DATE_CREATED_DB, sqlDate2);
        thread2.addAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread2, ThreadCommand.THREAD_COLLECTION_NAME);

        thread2 = new BaseDocument();
        thread2.setKey(parentThreadId3);
        thread2.addAttribute(ThreadCommand.CREATOR_ID_DB, mantaId);
        thread2.addAttribute(ThreadCommand.DESCRIPTION_DB, "tany agmad subreddit fl wogod");
        java.sql.Date sqlDate3 = new java.sql.Date(System.currentTimeMillis());
        thread2.addAttribute(ThreadCommand.DATE_CREATED_DB, sqlDate3);
        thread2.addAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread2, ThreadCommand.THREAD_COLLECTION_NAME);
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

    public static void followThread(String threadName, String actionMakerId) {
        BaseEdgeDocument follow = new BaseEdgeDocument();
        follow.setFrom(ThreadCommand.USER_COLLECTION_NAME + "/" + actionMakerId);
        follow.setTo(ThreadCommand.THREAD_COLLECTION_NAME + "/" + threadName);

        addObjectToEdgeCollection(follow, ThreadCommand.USER_FOLLOW_THREAD_COLLECTION_NAME);
    }

    public static String getFollowedThreads(String actionMakerId) {
        GetFollowedThreads getFollowedThreads = new GetFollowedThreads();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);
        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, actionMakerId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return getFollowedThreads.execute(request);
    }

    @Test
    public void T01_GetFollowedThreads() {
        arango.connectIfNotConnected();
        String response = getFollowedThreads(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(2, dataArr.length());
        for (int i = 0; i < 2; i++)
            assertEquals(lujineId, ((JSONObject) dataArr.get(i)).getString(ThreadCommand.CREATOR_ID_DB));
    }

    @Test
    public void T02_GetFollowedThreads() {
        arango.connectIfNotConnected();
        String response = getFollowedThreads(lujineId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(1, dataArr.length());
        assertEquals(mantaId, ((JSONObject) dataArr.get(0)).getString(ThreadCommand.CREATOR_ID_DB));
    }

    @Test
    public void T03_GetFollowedThreads() {
        arango.connectIfNotConnected();
        String response = getFollowedThreads(moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(0, dataArr.length());
    }
}
