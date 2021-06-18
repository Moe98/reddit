package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.auth.AuthParamsHandler;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.service.validation.HTTPMethod;

import static org.junit.Assert.*;

public class BookmarkSubThreadTest {
    // thread attribs
    protected static final String THREAD_DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String THREAD_CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String THREAD_NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String THREAD_DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();
    // db attribs
    final static String DB_NAME = SubThreadCommand.DB_Name;
    // collections
    final static String THREAD_COLLECTION_NAME = SubThreadCommand.THREAD_COLLECTION_NAME;
    final static String USER_COLLECTION_NAME = SubThreadCommand.USER_COLLECTION_NAME;
    final static String SUBTHREAD_COLLECTION_NAME = SubThreadCommand.SUBTHREAD_COLLECTION_NAME;
    final static String USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME = SubThreadCommand.USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME;
    // user attributes
    final static String USR_IS_DELETED = UserAttributes.IS_DELETED.getArangoDb();
    final static String USR_NUM_OF_FOLLOWERS = UserAttributes.NUM_OF_FOLLOWERS.getArangoDb();
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String fishName = "AsmakElRayes7amido", iceCreamName = "GelatiAzza";
    final private static String fishSubthreadId = "1234", iceCreamSubthreadId = "5678";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine,
            fishThread, iceCreamThread,
            fishSubThread, iceCreamSubThread;

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        arango.createDocument(SubThreadCommand.DB_Name, collectionName, document);
    }

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();

//            arango.dropDatabase(DB_NAME);
            arango.createDatabase(DB_NAME);

            arango.createCollection(DB_NAME, USER_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, THREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, SUBTHREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, true);

            moe = TestUtils.setUpUser(moeId, false, 0);
            TestUtils.addObjectToCollection(arango, moe, USER_COLLECTION_NAME);

            manta = TestUtils.setUpUser(mantaId, false, 0);
            ;
            TestUtils.addObjectToCollection(arango, manta, SubThreadCommand.USER_COLLECTION_NAME);

            lujine = TestUtils.setUpUser(lujineId, false, 0);
            TestUtils.addObjectToCollection(arango, lujine, SubThreadCommand.USER_COLLECTION_NAME);

            fishThread = TestUtils.setUpThread(fishName, mantaId, 0, "I love Asmak El Rayes 7amido");
            TestUtils.addObjectToCollection(arango, fishThread, THREAD_COLLECTION_NAME);

            iceCreamThread = TestUtils.setUpThread(iceCreamName, lujineId, 0, "I love Gelati Azza");
            TestUtils.addObjectToCollection(arango, iceCreamThread, THREAD_COLLECTION_NAME);

            fishSubThread = TestUtils.setUpSubThreadNoImage(fishSubthreadId, fishName, mantaId, "First Subthread",
                    "Fish is Love. Fish is life", 0, 0);
            TestUtils.addObjectToCollection(arango, fishSubThread, SUBTHREAD_COLLECTION_NAME);

            iceCreamSubThread = TestUtils.setUpSubThreadNoImage(iceCreamSubthreadId, iceCreamName, lujineId, "Second Subthread",
                    "Gelati is Love. Gelati is life", 0, 0);
            TestUtils.addObjectToCollection(arango, iceCreamSubThread, SUBTHREAD_COLLECTION_NAME);

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @AfterClass
    public static void tearDown() {

        arango.dropDatabase(SubThreadCommand.DB_Name);
    }

    private static JSONObject bookmarkSubThread(String userId, String subthreadId) {
        JSONObject body = new JSONObject();
        body.put(SubThreadAttributes.SUBTHREAD_ID.getHTTP(), subthreadId);

        JSONObject uriParams = new JSONObject();

        JSONObject request = TestUtils.makeRequest(body, uriParams, HTTPMethod.PUT);

        JSONObject claims = new JSONObject().put(SubThreadCommand.USERNAME, userId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        BookmarkSubThread bookmarkSubThread = new BookmarkSubThread();

        return new JSONObject(bookmarkSubThread.execute(request));
    }

    private static JSONObject unbookmarkSubThread(String userId, String subthreadId) {
        return bookmarkSubThread(userId, subthreadId);
    }

    // TODO can user bookmark their own subthread?
    @Test
    public void T01_UserBookmarkUnbookmarksSubThreads() {

        // manta bookmarks both subThreads
        JSONObject response = bookmarkSubThread(mantaId, fishSubthreadId);
        assertEquals(200, response.getInt("statusCode"));

        assertTrue(edgeExistsFromUserToSubThread(
                SubThreadCommand.USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                mantaId,
                fishSubthreadId));

        response = bookmarkSubThread(mantaId, iceCreamSubthreadId);
        assertEquals(200, response.getInt("statusCode"));
        assertTrue(edgeExistsFromUserToSubThread(
                SubThreadCommand.USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                mantaId,
                iceCreamSubthreadId));

        // TODO add assert message

        // manta unbookmarks both
        response = unbookmarkSubThread(mantaId, fishSubthreadId);
        assertEquals(200, response.getInt("statusCode"));

        assertFalse(edgeExistsFromUserToSubThread(
                SubThreadCommand.USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                mantaId,
                fishSubthreadId));

        response = unbookmarkSubThread(mantaId, iceCreamSubthreadId);
        assertEquals(200, response.getInt("statusCode"));
        assertFalse(edgeExistsFromUserToSubThread(
                SubThreadCommand.USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                mantaId,
                iceCreamSubthreadId));
    }

    @Test
    public void T02_UserBookmarkOneSubThreads() {

        // lujine bookmarks one subthread
        JSONObject response = bookmarkSubThread(lujineId, fishSubthreadId);
        assertEquals(200, response.getInt("statusCode"));

        assertTrue(edgeExistsFromUserToSubThread(
                SubThreadCommand.USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                lujineId,
                fishSubthreadId));

        assertFalse(edgeExistsFromUserToSubThread(
                SubThreadCommand.USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                lujineId,
                iceCreamSubthreadId));

        // TODO add assert message

    }

    @Test
    public void T03_UserBookmarkNonexistentSubthread() {

        // moe bookmarks nonexistent subthread
        JSONObject response = bookmarkSubThread(moeId, "1");
        assertEquals(400, response.getInt("statusCode"));

        assertEquals("Subthread does not exist", response.getString("msg"));

    }

    private boolean edgeExistsFromUserToSubThread(String edgeCollectionName, String userId, String subthreadId) {
        final String edgeId = arango.getSingleEdgeId(DB_NAME, edgeCollectionName,
                SubThreadCommand.USER_COLLECTION_NAME + "/" + userId,
                SubThreadCommand.SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

        return !edgeId.equals("");
    }
}
