package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.couchbase.Couchbase;
import org.sab.models.SubThreadAttributes;
import org.sab.service.validation.HTTPMethod;
import org.sab.subthread.SubThreadApp;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class UpdateSubThreadTest {

    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String fishName = "AsmakElRayes7amido";
    final private static String fishSubthread1Id = "1234", fishSubthread2Id = "5678", fishSubthread3Id = "91011";
    private static Arango arango;

    private static BaseDocument moe, manta, lujine,
            fishThread,
            fishSubThread1, fishSubThread2, fishSubThread3;


    // db attribs
    final static String DB_NAME = SubThreadCommand.DB_Name;

    // collections
    final static String THREAD_COLLECTION_NAME = SubThreadCommand.THREAD_COLLECTION_NAME;
    final static String USER_COLLECTION_NAME = SubThreadCommand.USER_COLLECTION_NAME;
    final static String SUBTHREAD_COLLECTION_NAME = SubThreadCommand.SUBTHREAD_COLLECTION_NAME;
    final static String COMMENT_COLLECTION_NAME = CommentCommand.COMMENT_COLLECTION_NAME;


    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getConnectedInstance();
            SubThreadApp.startCouchbaseConnection();

//            arango.dropDatabase(DB_NAME);
            arango.createDatabase(DB_NAME);

            arango.createCollection(DB_NAME, USER_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, THREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, SUBTHREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, COMMENT_COLLECTION_NAME, false);

            moe = TestUtils.setUpUser(moeId, false, 0);
            TestUtils.addObjectToCollection(arango, moe, USER_COLLECTION_NAME);

            manta = TestUtils.setUpUser(mantaId, false, 0);
            
            TestUtils.addObjectToCollection(arango, manta, USER_COLLECTION_NAME);

            lujine = TestUtils.setUpUser(lujineId, false, 0);
            TestUtils.addObjectToCollection(arango, lujine, USER_COLLECTION_NAME);

            fishThread = TestUtils.setUpThread(fishName, mantaId, 0, "I love Asmak El Rayes 7amido");
            TestUtils.addObjectToCollection(arango, fishThread, THREAD_COLLECTION_NAME);

            fishSubThread1 = TestUtils.setUpSubThreadNoImage(fishSubthread1Id, fishName, mantaId, "First Subthread",
                    "Fish is Love. Fish is life", 0, 0);
            fishSubThread1 = TestUtils.addObjectToCollection(arango, fishSubThread1, SUBTHREAD_COLLECTION_NAME);
//            fishSubThread1 = TestUtils.addObjectToCollection(arango, comm, COMMENT_COLLECTION_NAME);


            fishSubThread2 = TestUtils.setUpSubThreadNoImage(fishSubthread2Id, fishName, lujineId, "Second Subthread",
                    "Fish sucks. Fish sucks", 0, 0);
            fishSubThread2 = TestUtils.addObjectToCollection(arango, fishSubThread2, SUBTHREAD_COLLECTION_NAME);

            fishSubThread3 = TestUtils.setUpSubThreadNoImage(fishSubthread3Id, fishName, moeId, "Third Subthread",
                    "Fish sucks. Fish sucks", 0, 0);
            fishSubThread3 = TestUtils.addObjectToCollection(arango, fishSubThread3, SUBTHREAD_COLLECTION_NAME);

        } 
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void T01_NonCreatorUpdateSubThread() {
        JSONObject response = updateSubThread(fishSubthread1Id,  moeId, "malicious update", "malicious update");
        assertEquals(403, response.getInt("statusCode"));
        assertEquals(SubThreadCommand.REQUESTER_NOT_AUTHOR, response.getString("msg"));

        response = updateSubThread(fishSubthread2Id, moeId, "malicious update", "malicious update");
        assertEquals(403, response.getInt("statusCode"));
        assertEquals(SubThreadCommand.REQUESTER_NOT_AUTHOR, response.getString("msg"));

    }

    @Test
    public void T02_DeleteNonexistentSubThread() {
        JSONObject response = updateSubThread("lala", moeId, "malicious update", "malicious update");
        assertEquals(404, response.getInt("statusCode"));
        assertEquals(SubThreadCommand.OBJECT_NOT_FOUND, response.getString("msg"));
    }

    @Test
    public void T03_CreatorUpdateSubThread() {
        String updatedContent = "Updated Content";
        String updatedTitle = "Updated Title";

        JSONObject response = updateSubThread(fishSubthread1Id, mantaId, updatedContent, updatedTitle);
        assertEquals(200, response.getInt("statusCode"));

        JSONObject updatedSubthread = response.getJSONObject("data");

        validateSubthreadBase(updatedSubthread, fishSubThread1, true, true);
        assertEquals(
                updatedTitle,
                updatedSubthread.getString(SubThreadAttributes.TITLE.getDb()));
        assertEquals(
                updatedContent,
                updatedSubthread.getString(SubThreadAttributes.CONTENT.getDb()));
    }

    @Test
    public void T04_CreatorUpdateSubThreadTitle() {
        String updatedTitle = "Updated Title";

        JSONObject response = updateSubThreadTitle(fishSubthread2Id, lujineId, updatedTitle);
        assertEquals(200, response.getInt("statusCode"));

        JSONObject updatedSubthread = response.getJSONObject("data");

        validateSubthreadBase(updatedSubthread, fishSubThread2, true, false);
        assertEquals(
                updatedTitle,
                updatedSubthread.getString(SubThreadAttributes.TITLE.getDb()));

    }


    @Test
    public void T05_CreatorUpdateSubThreadContent() {
        String updatedContent = "Updated Content";

        JSONObject response = updateSubThreadContent(fishSubthread3Id, moeId, updatedContent);
        assertEquals(200, response.getInt("statusCode"));

        JSONObject updatedSubthread = response.getJSONObject("data");

        validateSubthreadBase(updatedSubthread, fishSubThread3, false, true);
        assertEquals(
                updatedContent,
                updatedSubthread.getString(SubThreadAttributes.CONTENT.getDb()));
    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(DB_NAME);
        Couchbase.getInstance().disconnect();
    }

    private static void validateSubthreadBase(JSONObject updatedSubthread, BaseDocument oldSubthread, boolean titleUpdated, boolean contentUpdated ) {
         assertEquals(
                 oldSubthread.getAttribute(SubThreadAttributes.DATE_CREATED.getDb()),
                 updatedSubthread.getString(SubThreadAttributes.DATE_CREATED.getDb()));
        assertEquals(
                oldSubthread.getAttribute(SubThreadAttributes.CREATOR_ID.getDb()),
                updatedSubthread.getString(SubThreadAttributes.CREATOR_ID.getDb()));
        assertEquals(
                oldSubthread.getAttribute(SubThreadAttributes.DISLIKES.getDb()),
                updatedSubthread.getInt(SubThreadAttributes.DISLIKES.getDb()));
        assertEquals(
                oldSubthread.getAttribute(SubThreadAttributes.LIKES.getDb()),
                updatedSubthread.getInt(SubThreadAttributes.LIKES.getDb()));
        assertEquals(
                oldSubthread.getAttribute(SubThreadAttributes.HAS_IMAGE.getDb()),
                updatedSubthread.getBoolean(SubThreadAttributes.HAS_IMAGE.getDb()));
        assertEquals(
                oldSubthread.getAttribute(SubThreadAttributes.PARENT_THREAD_ID.getDb()),
                updatedSubthread.getString(SubThreadAttributes.PARENT_THREAD_ID.getDb()));
        if(!contentUpdated) {
            assertEquals(
                    oldSubthread.getAttribute(SubThreadAttributes.CONTENT.getDb()),
                    updatedSubthread.getString(SubThreadAttributes.CONTENT.getDb()));
        }
        if(!titleUpdated) {
            assertEquals(
                oldSubthread.getAttribute(SubThreadAttributes.TITLE.getDb()),
                updatedSubthread.getString(SubThreadAttributes.TITLE.getDb()));

        }
    }

    private static JSONObject updateSubThreadTitle(String subthreadId, String userId, String title) {
        JSONObject body = new JSONObject();
        body.put(SubThreadCommand.TITLE, title);

        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadCommand.SUBTHREAD_ID, subthreadId);

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.PUT);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        UpdateSubThread updateSubThread = new UpdateSubThread();
        return new JSONObject(updateSubThread.execute(request));
    }

    private static JSONObject updateSubThreadContent(String subthreadId, String userId, String content) {
        JSONObject body = new JSONObject();
        body.put(SubThreadCommand.CONTENT, content);

        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadCommand.SUBTHREAD_ID, subthreadId);

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.PUT);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        UpdateSubThread updateSubThread = new UpdateSubThread();
        return new JSONObject(updateSubThread.execute(request));
    }

    private static JSONObject updateSubThread(String subthreadId, String userId, String content, String title) {

        JSONObject body = new JSONObject();
        body.put(SubThreadCommand.CONTENT, content);
        body.put(SubThreadCommand.TITLE, title);

        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadCommand.SUBTHREAD_ID, subthreadId);

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.PUT);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        UpdateSubThread updateSubThread = new UpdateSubThread();
        return new JSONObject(updateSubThread.execute(request));
    }

}
