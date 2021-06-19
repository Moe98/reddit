package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.couchbase.Couchbase;
import org.sab.service.validation.HTTPMethod;

import java.util.ArrayList;

import static org.junit.Assert.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeleteSubThreadTest {

    // db attribs
    final static String DB_NAME = SubThreadCommand.DB_Name;
    // collections
    final static String THREAD_COLLECTION_NAME = SubThreadCommand.THREAD_COLLECTION_NAME;
    final static String USER_COLLECTION_NAME = SubThreadCommand.USER_COLLECTION_NAME;
    final static String SUBTHREAD_COLLECTION_NAME = SubThreadCommand.SUBTHREAD_COLLECTION_NAME;
    final static String COMMENT_COLLECTION_NAME = CommentCommand.COMMENT_COLLECTION_NAME;
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String fishName = "AsmakElRayes7amido";
    final private static String fishSubthread1Id = "1234", fishSubthread2Id = "5678";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine,
            fishThread,
            fishSubThread1, fishSubThread2;
    private static ArrayList<String> s1Level1Comm, s1Level2Comm, s1Level3Comm;
    private static ArrayList<String> s2Level1Comm, s2Level2Comm, s2Level3Comm;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();

//            arango.dropDatabase(DB_NAME);
            arango.createDatabase(DB_NAME);
            Couchbase.getInstance().connectIfNotConnected();

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

            fishSubThread2 = TestUtils.setUpSubThreadNoImage(fishSubthread2Id, fishName, lujineId, "Second Subthread",
                    "Fish sucks. Fish sucks", 0, 0);
            TestUtils.addObjectToCollection(arango, fishSubThread2, SUBTHREAD_COLLECTION_NAME);

            // insert comments under fish subthread 1
            s1Level1Comm = insertFisrtLevelComments(fishSubthread1Id, lujineId, 5);
            s1Level2Comm = new ArrayList<>();
            s1Level3Comm = new ArrayList<>();

            for (String commId : s1Level1Comm) {
                s1Level2Comm.addAll(insertNestedComments(commId, moeId, 3));
            }
            for (String commId : s1Level2Comm) {
                s1Level3Comm.addAll(insertNestedComments(commId, mantaId, 2));
            }

            // insert comments under fish subthread 2
            s2Level1Comm = insertFisrtLevelComments(fishSubthread2Id, moeId, 5);
            s2Level2Comm = new ArrayList<>();
            s2Level3Comm = new ArrayList<>();

            for (String commId : s2Level1Comm) {
                s2Level2Comm.addAll(insertNestedComments(commId, mantaId, 3));
            }
            for (String commId : s2Level2Comm) {
                s2Level3Comm.addAll(insertNestedComments(commId, lujineId, 2));
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

    private static JSONObject deleteSubThread(String subthreadId, String userId) {

        JSONObject body = new JSONObject();
        body.put(SubThreadCommand.SUBTHREAD_ID, subthreadId);

        JSONObject uriParams = new JSONObject();

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.DELETE);

        JSONObject claims = new JSONObject().put(SubThreadCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        
        DeleteSubThread deletesubThread = new DeleteSubThread();
        return new JSONObject(deletesubThread.execute(request));
    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(DB_NAME);
    }

    @Test
    public void T01_NonCreatorDeleteSubThread() {
        JSONObject response = deleteSubThread(fishSubthread1Id, moeId);
        assertEquals(401, response.getInt("statusCode"));
        assertEquals("You are not authorized to delete this subthread!", response.getString("msg"));

        response = deleteSubThread(fishSubthread2Id, moeId);
        assertEquals(401, response.getInt("statusCode"));
        assertEquals("You are not authorized to delete this subthread!", response.getString("msg"));

    }

    @Test
    public void T02_DeleteNonexistentSubThread() {
        JSONObject response = deleteSubThread("lala", moeId);
        assertEquals(400, response.getInt("statusCode"));
        assertEquals("Subthread does not exist", response.getString("msg"));
    }

    @Test
    public void T03_CreatorDeleteSubThread() {
        JSONObject response = deleteSubThread(fishSubthread1Id, mantaId);
        assertEquals(200, response.getInt("statusCode"));

        assertTrue(arango.documentExists(DB_NAME, THREAD_COLLECTION_NAME, fishName));

        assertFalse(arango.documentExists(DB_NAME, SUBTHREAD_COLLECTION_NAME, fishSubthread1Id));
        assertTrue(arango.documentExists(DB_NAME, SUBTHREAD_COLLECTION_NAME, fishSubthread2Id));

        ArrayList<String> allDeletedComments = new ArrayList<String>();
        allDeletedComments.addAll(s1Level1Comm);
        allDeletedComments.addAll(s1Level2Comm);
        allDeletedComments.addAll(s1Level3Comm);
        ArrayList<String> allRemComments = new ArrayList<String>();
        allRemComments.addAll(s2Level1Comm);
        allRemComments.addAll(s2Level2Comm);
        allRemComments.addAll(s2Level3Comm);
        for (String commentId : allDeletedComments) {
            assertFalse(arango.documentExists(DB_NAME, COMMENT_COLLECTION_NAME, commentId));
        }
        for (String commentId : allRemComments) {
            assertTrue(arango.documentExists(DB_NAME, COMMENT_COLLECTION_NAME, commentId));
        }

    }

}

