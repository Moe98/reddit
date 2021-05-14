package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LikeSubThreadTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String parentThreadId1 = "asmakElRayes7amido", title1 = "gelaty azza is better", content1 = "fish is ya3", hasImage1 = "false";
    final private static String parentThreadId2 = "GelatiAzza", title2 = "fish is better", content2 = "fish is better", hasImage2 = "false";
    final private static String subthreadId1 = "20001", subthreadId2 = "20002", subthreadId3 = "20003";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            arango.createDatabaseIfNotExists(SubThreadCommand.TEST_DB_Name);
            createUsers();
            createSubThread(subthreadId1, parentThreadId1, content1, mantaId, title1, hasImage1);
            createSubThread(subthreadId2, parentThreadId1, content1, moeId, title1, hasImage1);
            createSubThread(subthreadId3, parentThreadId2, content2, moeId, title2, hasImage2);
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

    private static void addObjectToEdgeCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        if (!arango.collectionExists(SubThreadCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(SubThreadCommand.TEST_DB_Name, collectionName, true);
        }

        arango.createDocument(SubThreadCommand.TEST_DB_Name, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(SubThreadCommand.TEST_DB_Name, collectionName, document.getKey());
    }

    @AfterClass
    public static void tearDown() {
        arango.connectIfNotConnected();
        arango.dropDatabase(SubThreadCommand.TEST_DB_Name);
    }

    public static void createUsers() {
        moe = new BaseDocument();
        moe.setKey(moeId);
        moe.addAttribute(SubThreadCommand.USER_IS_DELETED_DB, false);
        moe.addAttribute(SubThreadCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(moe, SubThreadCommand.USER_COLLECTION_NAME);

        manta = new BaseDocument();
        manta.setKey(mantaId);
        manta.addAttribute(SubThreadCommand.USER_IS_DELETED_DB, false);
        manta.addAttribute(SubThreadCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(manta, SubThreadCommand.USER_COLLECTION_NAME);

        lujine = new BaseDocument();
        lujine.setKey(lujineId);
        lujine.addAttribute(SubThreadCommand.USER_IS_DELETED_DB, false);
        lujine.addAttribute(SubThreadCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(lujine, SubThreadCommand.USER_COLLECTION_NAME);
    }

    public static void createSubThread(String subThreadId, String parentThreadId, String content, String creatorId, String title, String hasImage) {

        BaseDocument comment = new BaseDocument();
        comment.setKey(subThreadId);
        comment.addAttribute(SubThreadCommand.PARENT_THREAD_ID_DB, parentThreadId);
        comment.addAttribute(SubThreadCommand.CREATOR_ID_DB, creatorId);
        comment.addAttribute(SubThreadCommand.CONTENT_DB, content);
        comment.addAttribute(SubThreadCommand.TITLE_DB, title);
        comment.addAttribute(SubThreadCommand.LIKES_DB, 0);
        comment.addAttribute(SubThreadCommand.DISLIKES_DB, 0);
        comment.addAttribute(SubThreadCommand.HASIMAGE_DB, hasImage);
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        comment.addAttribute(SubThreadCommand.DATE_CREATED_DB, sqlDate2);

        addObjectToCollection(comment, SubThreadCommand.SUBTHREAD_COLLECTION_NAME);
    }

    public static void dislikeSubthread(String userId, String subthreadId) {
        BaseEdgeDocument like = new BaseEdgeDocument();
        like.setFrom(SubThreadCommand.USER_COLLECTION_NAME + "/" + userId);
        like.setTo(SubThreadCommand.SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

        addObjectToEdgeCollection(like, SubThreadCommand.USER_DISLIKE_SUBTHREAD_COLLECTION_NAME);

        BaseDocument subthread = arango.readDocument(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_COLLECTION_NAME, subthreadId);
        int dislikes = Integer.parseInt(String.valueOf(subthread.getAttribute(SubThreadCommand.DISLIKES_DB)));
        subthread.updateAttribute(SubThreadCommand.DISLIKES_DB, dislikes + 1);
        arango.updateDocument(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_COLLECTION_NAME, subthread, subthreadId);
    }

    public static String likeSubthread(String userId, String subthreadId) {
        LikeSubThread likeSubThread = new LikeSubThread();
        JSONObject body = new JSONObject();
        body.put(SubThreadCommand.SUBTHREAD_ID, subthreadId);

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        JSONObject claims = new JSONObject().put(SubThreadCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return likeSubThread.execute(request);
    }

    @Test
    public void T01_UserLikeSubthreadForTheFirstTime() {
        String subthreadId = subthreadId1;
        arango.connectIfNotConnected();
        arango.createCollectionIfNotExists(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME, true);

        BaseDocument subthreadBeforeLike = arango.readDocument(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_COLLECTION_NAME, subthreadId);
        int oldNumOfLikes = Integer.parseInt(String.valueOf(subthreadBeforeLike.getAttribute(SubThreadCommand.LIKES_DB)));
        int oldNumOfDislikes = Integer.parseInt(String.valueOf(subthreadBeforeLike.getAttribute(SubThreadCommand.DISLIKES_DB)));

        arango.connectIfNotConnected();
        String response = likeSubthread(mantaId, subthreadId);
        JSONObject responseJson = new JSONObject(response);

        // checking the response of the command
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONObject data = (JSONObject) (responseJson.get("data"));
        assertEquals("added your like on the subthread", data.get("msg"));

        // checking the db for the addition of the like edge between the user and comment
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME, SubThreadCommand.USER_COLLECTION_NAME + "/" + mantaId);
        ArrayList<String> subthreadAtt = new ArrayList<>();
        subthreadAtt.add(SubThreadCommand.PARENT_THREAD_ID_DB);
        subthreadAtt.add(SubThreadCommand.CREATOR_ID_DB);
        subthreadAtt.add(SubThreadCommand.TITLE_DB);
        subthreadAtt.add(SubThreadCommand.CONTENT_DB);
        subthreadAtt.add(SubThreadCommand.LIKES_DB);
        subthreadAtt.add(SubThreadCommand.DISLIKES_DB);
        JSONArray commentArr = arango.parseOutput(cursor, SubThreadCommand.SUBTHREAD_ID_DB, subthreadAtt);
        assertEquals(1, commentArr.length());
        assertEquals(subthreadId, ((JSONObject) commentArr.get(0)).get(SubThreadCommand.SUBTHREAD_ID_DB));

        BaseDocument subthreadAfterLike = arango.readDocument(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_COLLECTION_NAME, subthreadId);
        int newNumOfLikes = Integer.parseInt(String.valueOf(subthreadAfterLike.getAttribute(SubThreadCommand.LIKES_DB)));
        int newNumOfDislikes = Integer.parseInt(String.valueOf(subthreadAfterLike.getAttribute(SubThreadCommand.DISLIKES_DB)));

        assertEquals(newNumOfLikes, oldNumOfLikes + 1);
        assertEquals(newNumOfDislikes, oldNumOfDislikes);
        arango.dropCollection(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME);
    }

    @Test
    public void T02_UserLikeSubthreadForTheSecondTime() {
        String subthreadId = subthreadId2;
        arango.connectIfNotConnected();
        arango.createCollectionIfNotExists(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME, true);

        // like the subthread the first time
        likeSubthread(mantaId, subthreadId);

        BaseDocument subthreadBefore2ndLike = arango.readDocument(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_COLLECTION_NAME, subthreadId);
        int oldNumOfLikes = Integer.parseInt(String.valueOf(subthreadBefore2ndLike.getAttribute(SubThreadCommand.LIKES_DB)));
        int oldNumOfDislikes = Integer.parseInt(String.valueOf(subthreadBefore2ndLike.getAttribute(SubThreadCommand.DISLIKES_DB)));

        arango.connectIfNotConnected();
        String response = likeSubthread(mantaId, subthreadId);
        JSONObject responseJson = new JSONObject(response);

        // checking the response of the command
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONObject data = (JSONObject) (responseJson.get("data"));
        assertEquals("removed your like on the subthread", data.get("msg"));

        // checking the db for the removal of the like edge between the user and subthread
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME, SubThreadCommand.USER_COLLECTION_NAME + "/" + mantaId);
        ArrayList<String> subthreadAtt = new ArrayList<>();
        subthreadAtt.add(SubThreadCommand.PARENT_THREAD_ID_DB);
        subthreadAtt.add(SubThreadCommand.CREATOR_ID_DB);
        subthreadAtt.add(SubThreadCommand.TITLE_DB);
        subthreadAtt.add(SubThreadCommand.CONTENT_DB);
        subthreadAtt.add(SubThreadCommand.LIKES_DB);
        subthreadAtt.add(SubThreadCommand.DISLIKES_DB);
        JSONArray commentArr = arango.parseOutput(cursor, SubThreadCommand.SUBTHREAD_ID_DB, subthreadAtt);
        assertEquals(0, commentArr.length());

        BaseDocument subthreadAfter2ndLike = arango.readDocument(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_COLLECTION_NAME, subthreadId);
        int newNumOfLikes = Integer.parseInt(String.valueOf(subthreadAfter2ndLike.getAttribute(SubThreadCommand.LIKES_DB)));
        int newNumOfDislikes = Integer.parseInt(String.valueOf(subthreadAfter2ndLike.getAttribute(SubThreadCommand.DISLIKES_DB)));

        assertEquals(newNumOfLikes, oldNumOfLikes - 1);
        assertEquals(newNumOfDislikes, oldNumOfDislikes);
        arango.dropCollection(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME);
    }


    @Test
    public void T03_UserLikeSubthreadAfterDislikingIt() {
        String subthreadId = subthreadId3;
        arango.connectIfNotConnected();
        arango.createCollectionIfNotExists(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(SubThreadCommand.DB_Name, SubThreadCommand.USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, true);

        dislikeSubthread(mantaId, subthreadId);

        BaseDocument subthreadBeforeLike = arango.readDocument(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_COLLECTION_NAME, subthreadId);
        int oldNumOfLikes = Integer.parseInt(String.valueOf(subthreadBeforeLike.getAttribute(SubThreadCommand.LIKES_DB)));
        int oldNumOfDislikes = Integer.parseInt(String.valueOf(subthreadBeforeLike.getAttribute(SubThreadCommand.DISLIKES_DB)));

        arango.connectIfNotConnected();
        String response = likeSubthread(mantaId, subthreadId);
        JSONObject responseJson = new JSONObject(response);

        // checking the response of the command
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONObject data = (JSONObject) (responseJson.get("data"));
        assertEquals("added your like on the subthread & removed your dislike", data.get("msg"));

        // checking the db for the removal of the like edge between the user and subthread
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME, SubThreadCommand.USER_COLLECTION_NAME + "/" + mantaId);
        ArrayList<String> subthreadAtt = new ArrayList<>();
        subthreadAtt.add(SubThreadCommand.PARENT_THREAD_ID_DB);
        subthreadAtt.add(SubThreadCommand.CREATOR_ID_DB);
        subthreadAtt.add(SubThreadCommand.TITLE_DB);
        subthreadAtt.add(SubThreadCommand.CONTENT_DB);
        subthreadAtt.add(SubThreadCommand.LIKES_DB);
        subthreadAtt.add(SubThreadCommand.DISLIKES_DB);
        JSONArray commentArr = arango.parseOutput(cursor, SubThreadCommand.SUBTHREAD_ID_DB, subthreadAtt);
        assertEquals(1, commentArr.length());
        assertEquals(subthreadId, ((JSONObject) commentArr.get(0)).get(SubThreadCommand.SUBTHREAD_ID_DB));

        BaseDocument subthreadAfterLike = arango.readDocument(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_COLLECTION_NAME, subthreadId);
        int newNumOfLikes = Integer.parseInt(String.valueOf(subthreadAfterLike.getAttribute(SubThreadCommand.LIKES_DB)));
        int newNumOfDislikes = Integer.parseInt(String.valueOf(subthreadAfterLike.getAttribute(SubThreadCommand.DISLIKES_DB)));

        assertEquals(newNumOfLikes, oldNumOfLikes + 1);
        assertEquals(newNumOfDislikes, oldNumOfDislikes - 1);

        // checking the db for the removal of the dislike edge between the user and comment
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor2 = arango.filterEdgeCollection(SubThreadCommand.DB_Name, SubThreadCommand.USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, CommentCommand.USER_COLLECTION_NAME + "/" + mantaId);
        JSONArray subthreadArr2 = arango.parseOutput(cursor2, SubThreadCommand.SUBTHREAD_ID_DB, subthreadAtt);
        assertEquals(0, subthreadArr2.length());

        arango.dropCollection(SubThreadCommand.DB_Name, SubThreadCommand.USER_LIKE_SUBTHREAD_COLLECTION_NAME);
        arango.dropCollection(SubThreadCommand.DB_Name, SubThreadCommand.USER_DISLIKE_SUBTHREAD_COLLECTION_NAME);
    }
}
