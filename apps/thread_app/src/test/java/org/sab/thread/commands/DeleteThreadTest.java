package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.service.validation.HTTPMethod;

import java.util.ArrayList;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeleteThreadTest {
    // db attribs
    final static String DB_NAME = ThreadCommand.DB_Name;
    // collections
    final static String THREAD_COLLECTION_NAME = ThreadCommand.THREAD_COLLECTION_NAME;
    final static String USER_COLLECTION_NAME = ThreadCommand.USER_COLLECTION_NAME;
    final static String SUBTHREAD_COLLECTION_NAME = ThreadCommand.SUBTHREAD_COLLECTION_NAME;
    final static String COMMENT_COLLECTION_NAME = ThreadCommand.COMMENT_COLLECTION_NAME;
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
            arango.connectIfNotConnected();
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

    private static JSONObject deleteThread(String threadName, String userId) {

        JSONObject body = new JSONObject();
        body.put(ThreadCommand.THREAD_NAME, threadName);

        JSONObject uriParams = new JSONObject();

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.DELETE);

        DeleteThread deleteThread = new DeleteThread();
        JSONObject claims = new JSONObject().put(ThreadCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        return new JSONObject(deleteThread.execute(request));
    }

    @AfterClass
    public static void tearDown() {
        arango.disconnect();
        arango.dropDatabase(DB_NAME);
    }

    @Test
    public void T01_NonCreatorDeleteThread() {
        JSONObject response = deleteThread(fishName, moeId);
        assertEquals(401, response.getInt("statusCode"));
        assertEquals("You are not authorized to delete this thread!", response.getString("msg"));

    }

    @Test
    public void T02_DeleteNonexistentThread() {
        JSONObject response = deleteThread("lala", moeId);
        assertEquals(400, response.getInt("statusCode"));
        assertEquals("Thread does not exist", response.getString("msg"));

    }

    @Test
    public void T03_CreatorDeleteThread() {
        JSONObject response = deleteThread(fishName, mantaId);
        System.out.println(response.toString());
        assertEquals(200, response.getInt("statusCode"));

        assertFalse(arango.documentExists(DB_NAME, THREAD_COLLECTION_NAME, fishName));

        assertFalse(arango.documentExists(DB_NAME, SUBTHREAD_COLLECTION_NAME, fishSubthread1Id));
        assertFalse(arango.documentExists(DB_NAME, SUBTHREAD_COLLECTION_NAME, fishSubthread2Id));

        ArrayList<String> allComments = new ArrayList<String>();
        allComments.addAll(s1Level1Comm);
        allComments.addAll(s1Level2Comm);
        allComments.addAll(s1Level3Comm);
        allComments.addAll(s2Level1Comm);
        allComments.addAll(s2Level2Comm);
        allComments.addAll(s2Level3Comm);
        for (String commentId : allComments) {
            assertFalse(arango.documentExists(DB_NAME, COMMENT_COLLECTION_NAME, commentId));
        }

    }

}
