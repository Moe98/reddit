package org.sab.subthread.commands;

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

public class GetMyDislikedSubThreadsTest {
    final private static String parentThreadId1 = "asmakElRayes7amido";
    final private static String parentThreadId2 = "GelatiAzza";
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getConnectedInstance();

            arango.createDatabaseIfNotExists(CommentCommand.TEST_DB_Name);
            createUsers();
            insertSubthread("20301", parentThreadId1, mantaId, "title", "content");
            insertSubthread("20201", parentThreadId1, moeId, "title", "content");
            insertSubthread("20202", parentThreadId1, lujineId, "title", "content");
            insertSubthread("20203", parentThreadId2, mantaId, "title", "content");
            insertSubthread("20204", parentThreadId2, moeId, "title", "content");
            insertSubthread("20205", parentThreadId2, lujineId, "title", "content");

            dislikeSubthread(mantaId, "20301");
            dislikeSubthread(mantaId, "20201");
            dislikeSubthread(mantaId, "20202");
            dislikeSubthread(mantaId, "20204");
            dislikeSubthread(mantaId, "20205");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        if (!arango.collectionExists(SubThreadCommand.DB_Name, collectionName)) {
            arango.createCollection(SubThreadCommand.DB_Name, collectionName, false);
        }

        arango.createDocument(SubThreadCommand.DB_Name, collectionName, document);
    }

    private static void addObjectToEdgeCollection(BaseDocument document, String collectionName) {
        if (!arango.collectionExists(SubThreadCommand.DB_Name, collectionName)) {
            arango.createCollection(SubThreadCommand.DB_Name, collectionName, true);
        }

        arango.createDocument(SubThreadCommand.DB_Name, collectionName, document);
    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(SubThreadCommand.DB_Name);
    }

    public static void createUsers() {
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

    public static void insertSubthread(String subthreadId, String parentThreadId, String creatorId, String title, String content) {
        BaseDocument subThread = new BaseDocument();
        subThread.setKey(subthreadId);
        subThread.addAttribute(SubThreadCommand.PARENT_THREAD_ID_DB, parentThreadId);
        subThread.addAttribute(SubThreadCommand.CREATOR_ID_DB, creatorId);
        subThread.addAttribute(SubThreadCommand.TITLE_DB, title);
        subThread.addAttribute(SubThreadCommand.CONTENT_DB, content);
        subThread.addAttribute(SubThreadCommand.LIKES_DB, 0);
        subThread.addAttribute(SubThreadCommand.DISLIKES_DB, 0);
        subThread.addAttribute(SubThreadCommand.HAS_IMAGE_DB, 0);
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        subThread.addAttribute(SubThreadCommand.DATE_CREATED_DB, sqlDate2);

        addObjectToCollection(subThread, SubThreadCommand.SUBTHREAD_COLLECTION_NAME);
    }

    public static void dislikeSubthread(String userId, String subthreadId) {
        BaseEdgeDocument like = new BaseEdgeDocument();
        like.setFrom(SubThreadCommand.USER_COLLECTION_NAME + "/" + userId);
        like.setTo(SubThreadCommand.SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

        addObjectToEdgeCollection(like, SubThreadCommand.USER_DISLIKE_SUBTHREAD_COLLECTION_NAME);
    }

    public static String getMyDislikedSubThreads(String userId) {
        GetMyDislikedSubThreads GetMyDislikedSubThreads = new GetMyDislikedSubThreads();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        JSONObject claims = new JSONObject().put(SubThreadCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        return GetMyDislikedSubThreads.execute(request);
    }

    @Test
    public void T01_GetMyLikedSubthreads() {
        String response = getMyDislikedSubThreads(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(5, dataArr.length());
    }

    @Test
    public void T02_GetMyLikedSubthreads() {
        String response = getMyDislikedSubThreads(moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(0, dataArr.length());
    }
}
