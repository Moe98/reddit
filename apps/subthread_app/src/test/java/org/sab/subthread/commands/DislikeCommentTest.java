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

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DislikeCommentTest {
    final private static String parentThreadId1 = "asmakElRayes7amido";
    final private static String parentThreadId2 = "GelatiAzza";
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String commentId1 = "20301", commentId2 = "20201", commentId3 = "20202";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            assertTrue(arango.isConnected());
            arango.createDatabaseIfNotExists(CommentCommand.TEST_DB_Name);
            createUsers();
            insertComments(commentId1, parentThreadId1, "content", mantaId, "SubThread");
            insertComments(commentId2, parentThreadId2, "content", moeId, "SubThread");
            insertComments(commentId3, commentId2, "content", lujineId, "Comment");
            /**
             *         subthread1 -> comment(20301)
             *         subthread2 -> comment(20201) -> comment(20202)
             */
        } catch (Exception e) {
            System.out.println("failed");
            fail(e.getMessage());
        }
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        if (!arango.collectionExists(CommentCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(CommentCommand.TEST_DB_Name, collectionName, false);
        }

        arango.createDocument(CommentCommand.TEST_DB_Name, collectionName, document);
    }

    private static void addObjectToEdgeCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        if (!arango.collectionExists(CommentCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(CommentCommand.TEST_DB_Name, collectionName, true);
        }

        arango.createDocument(CommentCommand.TEST_DB_Name, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(CommentCommand.TEST_DB_Name, collectionName, document.getKey());
    }

    @AfterClass
    public static void tearDown() {
        arango.connectIfNotConnected();
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

    public static void likeComment(String userId, String commentId) {
        BaseEdgeDocument like = new BaseEdgeDocument();
        like.setFrom("User/" + userId);
        like.setTo("Comment/" + commentId);

        addObjectToEdgeCollection(like, CommentCommand.USER_LIKE_COMMENT_COLLECTION_NAME);

        BaseDocument comment = arango.readDocument(CommentCommand.DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, commentId);
        int likes = Integer.parseInt(String.valueOf(comment.getAttribute(CommentCommand.LIKES_DB)));
        comment.updateAttribute(CommentCommand.LIKES_DB, likes + 1);
        arango.updateDocument(CommentCommand.DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, comment, commentId);
    }

    public static String dislikeComment(String userId, String commentId) {
        DislikeComment dc = new DislikeComment();

        JSONObject body = new JSONObject();
        body.put(CommentCommand.COMMENT_ID, commentId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(CommentCommand.ACTION_MAKER_ID, userId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        return dc.execute(request);
    }

    @Test
    public void T01_UserDislikeCommentForTheFirstTime() {
        String commentId = commentId1;
        arango.connectIfNotConnected();
        arango.createCollectionIfNotExists(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME, true);

        BaseDocument commentBeforeDislike = arango.readDocument(CommentCommand.DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, commentId);
        int oldNumOfLikes = Integer.parseInt(String.valueOf(commentBeforeDislike.getAttribute(CommentCommand.LIKES_DB)));
        int oldNumOfDislikes = Integer.parseInt(String.valueOf(commentBeforeDislike.getAttribute(CommentCommand.DISLIKES_DB)));

        arango.connectIfNotConnected();
        String response = dislikeComment(mantaId, commentId);
        JSONObject responseJson = new JSONObject(response);

        // checking the response of the command
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONObject data = (JSONObject) (responseJson.get("data"));
        assertEquals("added your dislike on the comment", data.get("msg"));

        // checking the db for the addition of the dislike edge between the user and comment
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME, CommentCommand.USER_COLLECTION_NAME + "/" + mantaId);
        ArrayList<String> commentAtt = new ArrayList<>();
        commentAtt.add(CommentCommand.CREATOR_ID_DB);
        commentAtt.add(CommentCommand.CONTENT_DB);
        commentAtt.add(CommentCommand.PARENT_CONTENT_TYPE_DB);
        commentAtt.add(CommentCommand.LIKES_DB);
        commentAtt.add(CommentCommand.DISLIKES_DB);
        commentAtt.add(CommentCommand.DATE_CREATED_DB);
        JSONArray commentArr = arango.parseOutput(cursor, CommentCommand.COMMENT_ID, commentAtt);
        assertEquals(1, commentArr.length());
        assertEquals(commentId, ((JSONObject) commentArr.get(0)).get(CommentCommand.COMMENT_ID));

        BaseDocument commentAfterDisike = arango.readDocument(CommentCommand.DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, commentId);
        int newNumOfLikes = Integer.parseInt(String.valueOf(commentAfterDisike.getAttribute(CommentCommand.LIKES_DB)));
        int newNumOfDislikes = Integer.parseInt(String.valueOf(commentAfterDisike.getAttribute(CommentCommand.DISLIKES_DB)));

        assertEquals(newNumOfLikes, oldNumOfLikes);
        assertEquals(newNumOfDislikes, oldNumOfDislikes + 1);
        arango.dropCollection(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME);
    }

    @Test
    public void T02_UserDislikeCommentForTheSecondTime() {
        String commentId = commentId2;
        arango.connectIfNotConnected();
        arango.createCollectionIfNotExists(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME, true);

        // dislike the comment first time
        dislikeComment(mantaId, commentId);

        BaseDocument commentBefore2ndDisLike = arango.readDocument(CommentCommand.DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, commentId);
        int oldNumOfLikes = Integer.parseInt(String.valueOf(commentBefore2ndDisLike.getAttribute(CommentCommand.LIKES_DB)));
        int oldNumOfDislikes = Integer.parseInt(String.valueOf(commentBefore2ndDisLike.getAttribute(CommentCommand.DISLIKES_DB)));

        arango.connectIfNotConnected();
        String response = dislikeComment(mantaId, commentId);
        JSONObject responseJson = new JSONObject(response);

        // checking the response of the command
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONObject data = (JSONObject) (responseJson.get("data"));
        assertEquals("removed your dislike on the comment", data.get("msg"));

        // checking the db for the removal of the dislike edge between the user and comment
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME, CommentCommand.USER_COLLECTION_NAME + "/" + mantaId);
        ArrayList<String> commentAtt = new ArrayList<>();
        commentAtt.add(CommentCommand.CREATOR_ID_DB);
        commentAtt.add(CommentCommand.CONTENT_DB);
        commentAtt.add(CommentCommand.PARENT_CONTENT_TYPE_DB);
        commentAtt.add(CommentCommand.LIKES_DB);
        commentAtt.add(CommentCommand.DISLIKES_DB);
        commentAtt.add(CommentCommand.DATE_CREATED_DB);
        JSONArray commentArr = arango.parseOutput(cursor, CommentCommand.COMMENT_ID, commentAtt);
        assertEquals(0, commentArr.length());

        BaseDocument commentAfter2ndDislike = arango.readDocument(CommentCommand.DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, commentId);
        int newNumOfLikes = Integer.parseInt(String.valueOf(commentAfter2ndDislike.getAttribute(CommentCommand.LIKES_DB)));
        int newNumOfDislikes = Integer.parseInt(String.valueOf(commentAfter2ndDislike.getAttribute(CommentCommand.DISLIKES_DB)));

        assertEquals(newNumOfLikes, oldNumOfLikes);
        assertEquals(newNumOfDislikes, oldNumOfDislikes - 1);

        arango.dropCollection(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME);
    }

    @Test
    public void T03_UserDislikeCommentAfterLikingIt() {
        String commentId = commentId3;
        arango.connectIfNotConnected();
        arango.createCollectionIfNotExists(CommentCommand.DB_Name, CommentCommand.USER_LIKE_COMMENT_COLLECTION_NAME, true);
        arango.createCollectionIfNotExists(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME, true);

        likeComment(mantaId, commentId);

        BaseDocument commentBeforeDislike = arango.readDocument(CommentCommand.DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, commentId);
        int oldNumOfLikes = Integer.parseInt(String.valueOf(commentBeforeDislike.getAttribute(CommentCommand.LIKES_DB)));
        int oldNumOfDislikes = Integer.parseInt(String.valueOf(commentBeforeDislike.getAttribute(CommentCommand.DISLIKES_DB)));

        arango.connectIfNotConnected();
        String response = dislikeComment(mantaId, commentId);
        JSONObject responseJson = new JSONObject(response);

        // checking the response of the command
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONObject data = (JSONObject) (responseJson.get("data"));
        assertEquals("added your dislike on the comment & removed your like", data.get("msg"));

        // checking the db for the addition of the dislike edge between the user and comment
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME, CommentCommand.USER_COLLECTION_NAME + "/" + mantaId);
        ArrayList<String> commentAtt = new ArrayList<>();
        commentAtt.add(CommentCommand.CREATOR_ID_DB);
        commentAtt.add(CommentCommand.CONTENT_DB);
        commentAtt.add(CommentCommand.PARENT_CONTENT_TYPE_DB);
        commentAtt.add(CommentCommand.LIKES_DB);
        commentAtt.add(CommentCommand.DISLIKES_DB);
        commentAtt.add(CommentCommand.DATE_CREATED_DB);
        JSONArray commentArr = arango.parseOutput(cursor, CommentCommand.COMMENT_ID, commentAtt);
        assertEquals(1, commentArr.length());
        assertEquals(commentId, ((JSONObject) commentArr.get(0)).get(CommentCommand.COMMENT_ID));

        BaseDocument commentAfterDislike = arango.readDocument(CommentCommand.DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, commentId);
        int newNumOfLikes = Integer.parseInt(String.valueOf(commentAfterDislike.getAttribute(CommentCommand.LIKES_DB)));
        int newNumOfDislikes = Integer.parseInt(String.valueOf(commentAfterDislike.getAttribute(CommentCommand.DISLIKES_DB)));

        assertEquals(newNumOfLikes, oldNumOfLikes - 1);
        assertEquals(newNumOfDislikes, oldNumOfDislikes + 1);

        // checking the db for the removal of the like edge between the user and comment
        arango.connectIfNotConnected();
        ArangoCursor<BaseDocument> cursor2 = arango.filterEdgeCollection(CommentCommand.DB_Name, CommentCommand.USER_LIKE_COMMENT_COLLECTION_NAME, CommentCommand.USER_COLLECTION_NAME + "/" + mantaId);
        JSONArray commentArr2 = arango.parseOutput(cursor2, CommentCommand.COMMENT_ID, commentAtt);
        assertEquals(0, commentArr2.length());

        arango.dropCollection(CommentCommand.DB_Name, CommentCommand.USER_LIKE_COMMENT_COLLECTION_NAME);
        arango.dropCollection(CommentCommand.DB_Name, CommentCommand.USER_DISLIKE_COMMENT_COLLECTION_NAME);
    }
}
