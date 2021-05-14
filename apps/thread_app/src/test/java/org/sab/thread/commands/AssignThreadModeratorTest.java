package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.arango.Arango;
import org.sab.service.validation.HTTPMethod;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssignThreadModeratorTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String fishName = "AsmakElRayes7amido", iceCreamName = "GelatiAzza";

    private static Arango arango;
    private static BaseDocument moe, manta, lujine,
            fishThread, iceCreamThread;

    // db attribs
    final static String DB_NAME = ThreadCommand.DB_Name;

    // collections
    final static String THREAD_COLLECTION_NAME = ThreadCommand.THREAD_COLLECTION_NAME;
    final static String USER_COLLECTION_NAME = ThreadCommand.USER_COLLECTION_NAME;
    final static String USER_MOD_THREAD_COLLECTION_NAME = ThreadCommand.USER_MOD_THREAD_COLLECTION_NAME;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            arango.createDatabase(DB_NAME);

            arango.createCollection(DB_NAME, USER_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, THREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, USER_MOD_THREAD_COLLECTION_NAME, true);

            moe = TestUtils.setUpUser(moeId, false, 0);
            TestUtils.addObjectToCollection(arango, moe, USER_COLLECTION_NAME);

            manta = TestUtils.setUpUser(mantaId, false, 0);;
            TestUtils.addObjectToCollection(arango, manta, USER_COLLECTION_NAME);

            lujine = TestUtils.setUpUser(lujineId, false, 0);
            TestUtils.addObjectToCollection(arango, lujine, USER_COLLECTION_NAME);

            fishThread  = TestUtils.setUpThread(fishName, mantaId, 0, "I love Asmak El Rayes 7amido");
            TestUtils.addObjectToCollection(arango, fishThread, THREAD_COLLECTION_NAME);
            // assign creator to be mod
            BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
            edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + mantaId);
            edgeDocument.setTo(THREAD_COLLECTION_NAME + "/" + fishName);
            arango.createEdgeDocument(DB_NAME, USER_MOD_THREAD_COLLECTION_NAME, edgeDocument);

            iceCreamThread  = TestUtils.setUpThread(iceCreamName, lujineId, 0, "I love Gelati Azza");
            TestUtils.addObjectToCollection(arango, iceCreamThread, THREAD_COLLECTION_NAME);
            BaseEdgeDocument edgeDocument2 = new BaseEdgeDocument();
            edgeDocument2.setFrom(USER_COLLECTION_NAME + "/" + lujineId);
            edgeDocument2.setTo(THREAD_COLLECTION_NAME + "/" + iceCreamName);
            arango.createEdgeDocument(DB_NAME, USER_MOD_THREAD_COLLECTION_NAME, edgeDocument2);

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    private static JSONObject addMod(String threadName, String modId, String assignerId) {
        JSONObject body = new JSONObject();
        body.put(ThreadCommand.THREAD_NAME, threadName);
        body.put(ThreadCommand.MODERATOR_ID, modId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(ThreadCommand.ASSIGNER_ID, assignerId);

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.PUT);

        AssignThreadModerator assignThreadModerator = new AssignThreadModerator();

        return new JSONObject(assignThreadModerator.execute(request));
    }


    @Test
    public void T01_NonModUserAssignMod() {

        // moe assigns himself as mod of both threads
        JSONObject response = addMod(fishName, moeId, moeId);
        assertEquals(401, response.getInt("statusCode"));

        assertEquals("You don't have permission to assign a moderator for this thread", response.getString("msg"));

        response = addMod(iceCreamName, moeId, moeId);
        assertEquals(401, response.getInt("statusCode"));

        assertEquals("You don't have permission to assign a moderator for this thread", response.getString("msg"));

        // manta assigns moe mod of icecream
        response = addMod(iceCreamName, mantaId, moeId);
        assertEquals(401, response.getInt("statusCode"));

        assertEquals("You don't have permission to assign a moderator for this thread", response.getString("msg"));
    }

    @Test
    public void T02_UserAssignModNonexistentThread() {

        // moe assigns Lujine as mod of nonexistent thread
        JSONObject response = addMod("DoesntExist", lujineId, moeId);
        assertEquals(400, response.getInt("statusCode"));

        assertEquals("Thread does not exist", response.getString("msg"));

    }

    @Test
    public void T03_ModAssignMod() {

        // manta assigns moe as mod of fish
        JSONObject response = addMod(fishName, moeId, mantaId);
        assertEquals(200, response.getInt("statusCode"));

        assertTrue(edgeExistsFromUserToThread(
                USER_MOD_THREAD_COLLECTION_NAME,
                moeId,
                fishName));

        // moe assigns lujine as mod of fish
        response = addMod(fishName, lujineId, moeId);
        assertEquals(200, response.getInt("statusCode"));

        assertTrue(edgeExistsFromUserToThread(
                USER_MOD_THREAD_COLLECTION_NAME,
                lujineId,
                fishName));

        // lujine assigns moe as mod of icecream
        response = addMod(iceCreamName, moeId, lujineId);
        assertEquals(200, response.getInt("statusCode"));

        assertTrue(edgeExistsFromUserToThread(
                USER_MOD_THREAD_COLLECTION_NAME,
                moeId,
                iceCreamName));

    }

    @Test
    public void T04_UserAlreadyMod() {

        // manta assigns moe as mod of fish
        JSONObject response = addMod(fishName, moeId, mantaId);
        assertEquals(400, response.getInt("statusCode"));
        assertEquals("User already moderates this thread", response.getString("msg"));

        assertTrue(edgeExistsFromUserToThread(
                USER_MOD_THREAD_COLLECTION_NAME,
                moeId,
                fishName));

        // moe assigns lujine as mod of fish
        response = addMod(fishName, lujineId, moeId);
        assertEquals(400, response.getInt("statusCode"));
        assertEquals("User already moderates this thread", response.getString("msg"));

        assertTrue(edgeExistsFromUserToThread(
                USER_MOD_THREAD_COLLECTION_NAME,
                lujineId,
                fishName));

        // lujine assigns moe as mod of icecream
        response = addMod(iceCreamName, moeId, lujineId);
        assertEquals(400, response.getInt("statusCode"));
        assertEquals("User already moderates this thread", response.getString("msg"));

        assertTrue(edgeExistsFromUserToThread(
                USER_MOD_THREAD_COLLECTION_NAME,
                moeId,
                iceCreamName));

    }

    private boolean edgeExistsFromUserToThread(String edgeCollectionName, String userId, String threadId) {
        final String edgeId = arango.getSingleEdgeId(DB_NAME, edgeCollectionName,
                ThreadCommand.USER_COLLECTION_NAME + "/" + userId,
                ThreadCommand.THREAD_COLLECTION_NAME + "/" + threadId);

        return !edgeId.equals("");
    }

    @AfterClass
    public static void tearDown() {
        arango.disconnect();
        arango.dropDatabase(DB_NAME);
    }
}
