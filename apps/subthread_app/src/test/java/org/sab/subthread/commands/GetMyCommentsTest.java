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

            arango.createDatabaseIfNotExists(CommentCommand.TEST_DB_Name);
            createUsers();
            createThreads();
            createSubThread(subThreadId1, parentThreadId1, "content", mantaId, "SubThread", hasImage1);
            createSubThread(subThreadId2, parentThreadId1, "content", moeId, "SubThread", hasImage1);
            createComments(subThreadId1, "content", mantaId, "SubThread", 50);
            createComments(subThreadId2, "content", mantaId, "SubThread", 44);
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
            fail(e.getMessage());
        }
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        if (!arango.collectionExists(CommentCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(CommentCommand.TEST_DB_Name, collectionName, false);
        }

        arango.createDocument(CommentCommand.TEST_DB_Name, collectionName, document);
    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(CommentCommand.TEST_DB_Name);
    }

    public static void createUsers() {
        moe = new BaseDocument();
        moe.setKey(moeId);
        moe.addAttribute(CommentCommand.USER_IS_DELETED_DB, false);
        moe.addAttribute(CommentCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(moe, CommentCommand.USER_COLLECTION_NAME);

        manta = new BaseDocument();
        manta.setKey(mantaId);
        manta.addAttribute(CommentCommand.USER_IS_DELETED_DB, false);
        manta.addAttribute(CommentCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(manta, CommentCommand.USER_COLLECTION_NAME);

        lujine = new BaseDocument();
        lujine.setKey(lujineId);
        lujine.addAttribute(CommentCommand.USER_IS_DELETED_DB, false);
        lujine.addAttribute(CommentCommand.USER_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(lujine, CommentCommand.USER_COLLECTION_NAME);
    }

    public static void createThreads() {
        thread1 = new BaseDocument();
        thread1.setKey(parentThreadId1);
        thread1.addAttribute(CommentCommand.THREAD_CREATOR_ID_DB, mantaId);
        thread1.addAttribute(CommentCommand.THREAD_DESCRIPTION_DB, "agmad subreddit fl wogod");
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        thread1.addAttribute(CommentCommand.THREAD_DATE_CREATED_DB, sqlDate);
        thread1.addAttribute(CommentCommand.THREAD_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread1, CommentCommand.THREAD_COLLECTION_NAME);

        thread2 = new BaseDocument();
        thread2.setKey(parentThreadId2);
        thread2.addAttribute(CommentCommand.THREAD_CREATOR_ID_DB, moeId);
        thread2.addAttribute(CommentCommand.THREAD_DESCRIPTION_DB, "tany agmad subreddit fl wogod");
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        thread2.addAttribute(CommentCommand.THREAD_DATE_CREATED_DB, sqlDate2);
        thread2.addAttribute(CommentCommand.THREAD_NUM_OF_FOLLOWERS_DB, 0);
        addObjectToCollection(thread2, CommentCommand.THREAD_COLLECTION_NAME);
    }

    public static void createSubThread(String subThreadId, String parentThreadId, String content, String creatorId, String title, String hasImage) {

        BaseDocument comment = new BaseDocument();
        comment.setKey(subThreadId);
        comment.addAttribute(CommentCommand.SUBTHREAD_PARENT_THREAD_ID_DB, parentThreadId);
        comment.addAttribute(CommentCommand.SUBTHREAD_CREATOR_ID_DB, creatorId);
        comment.addAttribute(CommentCommand.SUBTHREAD_CONTENT_DB, content);
        comment.addAttribute(CommentCommand.SUBTHREAD_TITLE_DB, title);
        comment.addAttribute(CommentCommand.SUBTHREAD_LIKES_DB, 0);
        comment.addAttribute(CommentCommand.SUBTHREAD_DISLIKES_DB, 0);
        comment.addAttribute(CommentCommand.SUBTHREAD_HAS_IMAGE_DB, hasImage);
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        comment.addAttribute(CommentCommand.SUBTHREAD_DATE_CREATED_DB, sqlDate2);

        addObjectToCollection(comment, CommentCommand.SUBTHREAD_COLLECTION_NAME);
    }

    public static void insertComments(String commentId, String parentSubThreadId, String content, String creatorId, String parentContentType) {
        BaseDocument comment = new BaseDocument();
        comment.setKey(commentId);
        comment.addAttribute(CommentCommand.PARENT_SUBTHREAD_ID_DB, parentSubThreadId);
        comment.addAttribute(CommentCommand.CREATOR_ID_DB, creatorId);
        comment.addAttribute(CommentCommand.CONTENT_DB, content);
        comment.addAttribute(CommentCommand.PARENT_CONTENT_TYPE_DB, parentContentType);
        comment.addAttribute(CommentCommand.LIKES_DB, 0);
        comment.addAttribute(CommentCommand.DISLIKES_DB, 0);
        java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
        comment.addAttribute(CommentCommand.DATE_CREATED_DB, sqlDate2);

        addObjectToCollection(comment, CommentCommand.COMMENT_COLLECTION_NAME);
    }

    public static void createComments(String parentSubThreadId, String content, String creatorId, String parentContentType, int amount) {
        for (int i = 0; i < amount; i++) {
            BaseDocument comment = new BaseDocument();
            comment.addAttribute(CommentCommand.PARENT_SUBTHREAD_ID_DB, parentSubThreadId);
            comment.addAttribute(CommentCommand.CREATOR_ID_DB, creatorId);
            comment.addAttribute(CommentCommand.CONTENT_DB, content);
            comment.addAttribute(CommentCommand.PARENT_CONTENT_TYPE_DB, parentContentType);
            comment.addAttribute(CommentCommand.LIKES_DB, 0);
            comment.addAttribute(CommentCommand.DISLIKES_DB, 0);
            java.sql.Date sqlDate2 = new java.sql.Date(System.currentTimeMillis());
            comment.addAttribute(CommentCommand.DATE_CREATED_DB, sqlDate2);

            addObjectToCollection(comment, CommentCommand.COMMENT_COLLECTION_NAME);
        }
    }

    public static String getMyComments(String userId) {
        GetMyComments getMyComments = new GetMyComments();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(CommentCommand.USER_ID, userId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        return getMyComments.execute(request);
    }

    @Test
    public void T01_GetMyComments() {
        String response = getMyComments(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(100, dataArr.length());
        for (int i = 0; i < 100; i++)
            assertEquals(mantaId, ((JSONObject) dataArr.get(i)).getString(SubThreadCommand.CREATOR_ID_DB));
    }

    @Test
    public void T02_GetMyComments() {
        String response = getMyComments(moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(0, dataArr.length());
    }
}
