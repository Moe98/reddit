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

public class GetMyCommentsTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String parentThreadId1 = "asmakElRayes7amido", title1 = "gelaty azza is better", content1 = "fish is ya3", hasImage1 = "false";
    final private static String parentThreadId2 = "GelatiAzza", title2 = "fish is better", content2 = "fish is better", hasImage2 = "false";
    final private static String parentThreadId3 = "karateenBaraka", title3 = "water is better", content3 = "water", hasImage3 = "false";
    final private static String subThreadId1 = "22111", subThreadId2 = "22112";
    private static Arango arango;
    private static BaseDocument subthread, thread1, thread2, moe, manta, lujine;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            assertTrue(arango.isConnected());
            arango.createDatabaseIfNotExists(CommentCommand.TEST_DB_Name);
            createUsers();
            createThreads();
            createSubThread(subThreadId1,parentThreadId1,"content",mantaId,"SubThread",hasImage1);
            createSubThread(subThreadId2,parentThreadId1,"content",moeId,"SubThread",hasImage1);
            createComments(subThreadId1, "content", mantaId, "SubThread", 50) ;
            createComments(subThreadId2, "content", mantaId, "SubThread", 44) ;
            insertComments("20201", subThreadId2, "content", mantaId, "SubThread");
            insertComments("20202", "20201", "content", mantaId, "Comment");
            insertComments("20203", "20202", "content", mantaId, "Comment");
            insertComments("20204", "20202", "content", mantaId, "Comment");
            insertComments("20205", "20203", "content", mantaId, "Comment");
            insertComments("20206", "20204", "content", mantaId, "Comment");
            /**
             *         subthread1 -> 50 comment from manta
             *         subthread2 -> 44 comments from manta plus the below comments structure
             *         subthread2 -> comment(20201) -> comment(20202) |-> comment(20203) -> comment(20205)
             *                                                        |-> comment(20204) -> comment(20206)
             */
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

    public static void createSubThread(String subThreadId, String parentThreadId, String content, String creatorId, String parentContentType, String hasImage) {

        BaseDocument comment = new BaseDocument();
        comment.setKey(subThreadId);
        comment.addAttribute(CommentCommand.THREAD_PARENT_THREAD_ID_DB, parentThreadId);
        comment.addAttribute(CommentCommand.THREAD_CREATOR_ID_DB,  creatorId);
        comment.addAttribute(CommentCommand.THREAD_CONTENT_DB, content);
        comment.addAttribute(CommentCommand.THREAD_TITLE_DB, parentContentType);
        comment.addAttribute(CommentCommand.THREAD_LIKES_DB, 0);
        comment.addAttribute(CommentCommand.THREAD_DISLIKES_DB, 0);
        comment.addAttribute(CommentCommand.THREAD_HASIMAGE_DB, hasImage);
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        comment.addAttribute(CommentCommand.THREAD_DATE_CREATED_DB, sqlDate2);

        addObjectToCollection(comment, CommentCommand.SUBTHREAD_COLLECTION_NAME);
    }

    public static void insertComments(String commentId, String parentSubThreadId, String content, String creatorId, String parentContentType) {
        BaseDocument comment = new BaseDocument();
        comment.setKey(commentId);
        comment.addAttribute(CommentCommand.PARENT_SUBTHREAD_ID_DB, parentSubThreadId);
        comment.addAttribute(CommentCommand.CREATOR_ID_DB,  creatorId);
        comment.addAttribute(CommentCommand.CONTENT_DB, content);
        comment.addAttribute(CommentCommand.PARENT_CONTENT_TYPE_DB, parentContentType);
        comment.addAttribute(CommentCommand.LIKES_DB, 0);
        comment.addAttribute(CommentCommand.DISLIKES_DB, 0);
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        comment.addAttribute(CommentCommand.DATE_CREATED_DB, sqlDate2);

        addObjectToCollection(comment, CommentCommand.COMMENT_COLLECTION_NAME);
    }
    public static void createComments(String parentSubThreadId, String content, String creatorId, String parentContentType, int amount) {
        for(int i = 0; i < amount; i++) {
            BaseDocument comment = new BaseDocument();
            comment.addAttribute(CommentCommand.PARENT_SUBTHREAD_ID_DB, parentSubThreadId);
            comment.addAttribute(CommentCommand.CREATOR_ID_DB,  creatorId);
            comment.addAttribute(CommentCommand.CONTENT_DB, content);
            comment.addAttribute(CommentCommand.PARENT_CONTENT_TYPE_DB, parentContentType);
            comment.addAttribute(CommentCommand.LIKES_DB, 0);
            comment.addAttribute(CommentCommand.DISLIKES_DB, 0);
            java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
            comment.addAttribute(CommentCommand.DATE_CREATED_DB, sqlDate2);

            addObjectToCollection(comment, CommentCommand.COMMENT_COLLECTION_NAME);
        }
    }

    public static String getMyComments(String userId){
        GetMyComments getMyComments = new GetMyComments();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(CommentCommand.USER_ID, userId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        return getMyComments.execute(request);
    }

    @Test
    public void T01_GetMyComments() {
        arango.connectIfNotConnected();
        String response = getMyComments(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray)(responseJson.get("data"));
        assertEquals(100, dataArr.length());
        for(int i = 0; i<100;i++)
            assertEquals(mantaId, ((JSONObject)dataArr.get(i)).getString(SubThreadCommand.CREATOR_ID_DB));
    }

    @Test
    public void T02_GetMyComments() {
        arango.connectIfNotConnected();
        String response = getMyComments(moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray)(responseJson.get("data"));
        assertEquals(0, dataArr.length());
    }
}
