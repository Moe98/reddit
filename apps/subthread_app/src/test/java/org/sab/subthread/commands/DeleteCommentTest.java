package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DeleteCommentTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String fishName = "AsmakElRayes7amido";
    final private static String fishSubthread1Id = "1234";
    private static Arango arango;

    private static BaseDocument moe, manta, lujine,
            fishThread,
            fishSubThread1;

    private static ArrayList<String> s1Level1Comm, s1Level2Comm, s1Level3Comm;

    private static String commentWithChildren;
    private static ArrayList<String> children;

    // db attribs
    final static String DB_NAME = CommentCommand.DB_Name;

    // collections
    final static String THREAD_COLLECTION_NAME = CommentCommand.THREAD_COLLECTION_NAME;
    final static String USER_COLLECTION_NAME = CommentCommand.USER_COLLECTION_NAME;
    final static String SUBTHREAD_COLLECTION_NAME = CommentCommand.SUBTHREAD_COLLECTION_NAME;
    final static String COMMENT_COLLECTION_NAME = CommentCommand.COMMENT_COLLECTION_NAME;


    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            assertTrue(arango.isConnected());
            arango.createDatabase(DB_NAME);

            arango.createCollection(DB_NAME, USER_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, THREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, SUBTHREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, COMMENT_COLLECTION_NAME, false);

            moe = TestUtils.setUpUser(moeId, false, 0);
            TestUtils.addObjectToCollection(arango, moe, USER_COLLECTION_NAME);

            manta = TestUtils.setUpUser(mantaId, false, 0);
            ;
            TestUtils.addObjectToCollection(arango, manta, USER_COLLECTION_NAME);

            lujine = TestUtils.setUpUser(lujineId, false, 0);
            TestUtils.addObjectToCollection(arango, lujine, USER_COLLECTION_NAME);

            fishThread = TestUtils.setUpThread(fishName, mantaId, 0, "I love Asmak El Rayes 7amido");
            TestUtils.addObjectToCollection(arango, fishThread, THREAD_COLLECTION_NAME);

            fishSubThread1 = TestUtils.setUpSubThreadNoImage(fishSubthread1Id, fishName, mantaId, "First Subthread",
                    "Fish is Love. Fish is life", 0, 0);
            TestUtils.addObjectToCollection(arango, fishSubThread1, SUBTHREAD_COLLECTION_NAME);

            // insert comments under fish subthread 1
            s1Level1Comm = insertFisrtLevelComments(fishSubthread1Id, lujineId, 5);
            commentWithChildren = s1Level1Comm.get(0);
            children = new ArrayList<>();

            s1Level2Comm = new ArrayList<>();
            s1Level3Comm = new ArrayList<>();

            ArrayList<String> arr;
            for (String commId : s1Level1Comm) {
                arr = insertNestedComments(commId, moeId, 3);
                s1Level2Comm.addAll(arr);
                if(commId.equals(commentWithChildren)) {
                    children.addAll(arr);
                }
            }
            for (String commId : s1Level2Comm) {
                arr = insertNestedComments(commId, mantaId, 2);
                s1Level3Comm.addAll(arr);
                if(children.contains(commId)) {
                    children.addAll(arr);
                }
            }

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    private static ArrayList<String> insertFisrtLevelComments(String subthreadId, String userId, int numComments) {
        ArrayList<String> commentIds = new ArrayList<>();
        for (int i = 0; i < numComments; i++) {
            // insert comment
            BaseDocument comm = TestUtils.setUpComment(userId, subthreadId, "SubThread", "Content", 0, 0);
            BaseDocument res = TestUtils.addObjectToCollection(arango, comm, COMMENT_COLLECTION_NAME);
            final String commentId = res.getKey();
            commentIds.add(commentId);
        }
        return commentIds;
    }

    private static ArrayList<String> insertNestedComments(String parentCommentId, String userId, int numComments) {
        ArrayList<String> commentIds = new ArrayList<>();
        for (int i = 0; i < numComments; i++) {
            // insert comment
            BaseDocument comm = TestUtils.setUpComment(userId, parentCommentId, "Comment", "Content", 0, 0);
            BaseDocument res = TestUtils.addObjectToCollection(arango, comm, COMMENT_COLLECTION_NAME);
            final String commentId = res.getKey();
            commentIds.add(commentId);
        }
        return commentIds;
    }

    private static JSONObject deleteComment(String commentId, String userId) {

        JSONObject body = new JSONObject();
        body.put(CommentCommand.COMMENT_ID, commentId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(CommentCommand.ACTION_MAKER_ID, userId);

        JSONObject request = TestUtils.makePutRequest(body, uriParams);

        DeleteComment deleteComment = new DeleteComment();
        return new JSONObject(deleteComment.execute(request));
    }

    @Test
    public void T01_NonCreatorDeleteComment() {
        String commentByMantaId = s1Level3Comm.get(0);
        JSONObject response = deleteComment(commentByMantaId, moeId);
        assertEquals(401, response.getInt("statusCode"));
        assertEquals("You are not authorized to delete this comment!", response.getString("msg"));

        String commentByLujineId = s1Level1Comm.get(0);
        response = deleteComment(commentByLujineId, moeId);
        assertEquals(401, response.getInt("statusCode"));
        assertEquals("You are not authorized to delete this comment!", response.getString("msg"));

    }

    @Test
    public void T02_DeleteNonexistentComment() {
        JSONObject response = deleteComment("lala", moeId);
        assertEquals(400, response.getInt("statusCode"));
        assertEquals("Comment does not exist", response.getString("msg"));
    }

    @Test
    public void T03_CreatorDeleteLeafComment() {
        String level3CommentId = s1Level3Comm.get(0);
        JSONObject response = deleteComment(level3CommentId, mantaId);
        assertEquals(200, response.getInt("statusCode"));

        assertTrue(arango.documentExists(DB_NAME, THREAD_COLLECTION_NAME, fishName));

        assertTrue(arango.documentExists(DB_NAME, SUBTHREAD_COLLECTION_NAME, fishSubthread1Id));

        ArrayList<String> allDeletedComments = new ArrayList<String>();
        allDeletedComments.add(level3CommentId);
        ArrayList<String> allRemComments = new ArrayList<String>();
        allRemComments.addAll(s1Level1Comm);
        allRemComments.addAll(s1Level2Comm);
        allRemComments.addAll(s1Level3Comm);
        allRemComments.remove(level3CommentId);
        for (String commentId : allDeletedComments) {
            assertFalse(arango.documentExists(DB_NAME, COMMENT_COLLECTION_NAME, commentId));
        }
        for (String commentId : allRemComments) {
            assertTrue(arango.documentExists(DB_NAME, COMMENT_COLLECTION_NAME, commentId));
        }

    }

    @Test
    public void T04_CreatorDeleteParentComment() {
//        String level1CommentId = s1Level1Comm.get(0);
        JSONObject response = deleteComment(commentWithChildren, lujineId);
        assertEquals(200, response.getInt("statusCode"));

        assertTrue(arango.documentExists(DB_NAME, THREAD_COLLECTION_NAME, fishName));

        assertTrue(arango.documentExists(DB_NAME, SUBTHREAD_COLLECTION_NAME, fishSubthread1Id));

        ArrayList<String> allDeletedComments = new ArrayList<String>();
        allDeletedComments.add(commentWithChildren);
        allDeletedComments.addAll(children);

        ArrayList<String> allRemComments = new ArrayList<String>();
        allRemComments.addAll(s1Level1Comm);
        allRemComments.addAll(s1Level2Comm);
        allRemComments.addAll(s1Level3Comm);
        allRemComments.remove(commentWithChildren);
        allRemComments.removeAll(children);

        for (String commentId : allDeletedComments) {
            assertFalse(arango.documentExists(DB_NAME, COMMENT_COLLECTION_NAME, commentId));
        }
        for (String commentId : allRemComments) {
            assertTrue(arango.documentExists(DB_NAME, COMMENT_COLLECTION_NAME, commentId));
        }

    }

    @AfterClass
    public static void tearDown() {
        arango.disconnect();
        arango.dropDatabase(DB_NAME);
    }
}
