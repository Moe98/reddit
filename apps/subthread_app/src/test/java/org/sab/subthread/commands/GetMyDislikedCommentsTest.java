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

public class GetMyDislikedCommentsTest {
    final private static String parentThreadId1 = "asmakElRayes7amido";
    final private static String parentThreadId2 = "GelatiAzza";
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();

            arango.createDatabaseIfNotExists(CommentCommand.TEST_DB_Name);
            createUsers();
            insertComments("20301", parentThreadId1, "content", mantaId, "SubThread");
            insertComments("20201", parentThreadId2, "content", moeId, "SubThread");
            insertComments("20202", "20201", "content", lujineId, "Comment");
            insertComments("20203", "20202", "content", mantaId, "Comment");
            insertComments("20204", "20202", "content", moeId, "Comment");
            insertComments("20205", "20203", "content", lujineId, "Comment");
            insertComments("20206", "20204", "content", mantaId, "Comment");
            /**
             *         subthread1 -> 1 comment from manta
             *         subthread2 -> comment(20201) -> comment(20202) |-> comment(20203) -> comment(20205)
             *                                                        |-> comment(20204) -> comment(20206)
             */
            dislikeComment(mantaId, "20301");
            dislikeComment(mantaId, "20201");
            dislikeComment(mantaId, "20202");
            dislikeComment(mantaId, "20203");
            dislikeComment(mantaId, "20204");
            dislikeComment(mantaId, "20205");
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

        arango.createDocument(CommentCommand.TEST_DB_Name, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(CommentCommand.TEST_DB_Name, collectionName, document.getKey());
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

    public static void dislikeComment(String userId, String commentId) {
        BaseEdgeDocument like = new BaseEdgeDocument();
        like.setFrom("User/" + userId);
        like.setTo("Comment/" + commentId);

        addObjectToEdgeCollection(like, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME);
    }

    public static String getMyDislikedComments(String userId) {
        GetMyDislikedComments getMyDislikedComments = new GetMyDislikedComments();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        return getMyDislikedComments.execute(request);
    }

    @Test
    public void T01_GetMyLikedComments() {
        String response = getMyDislikedComments(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(6, dataArr.length());
    }

    @Test
    public void T02_GetMyLikedComments() {
        String response = getMyDislikedComments(moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));
        assertEquals(0, dataArr.length());
    }
}
