package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.models.CommentAttributes;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class CreateSubThreadTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    final private static String fishName = "AsmakElRayes7amido", iceCreamName = "GelatiAzza";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine, fishThread, iceCreamThread;

    // db attribs
    final static String DB_NAME = SubThreadCommand.DB_Name;

    // thread attribs
    protected static final String THREAD_DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String THREAD_CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String THREAD_NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String THREAD_DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();

    // user attributes
    final static String USR_IS_DELETED = UserAttributes.IS_DELETED.getDb();
    final static String USR_NUM_OF_FOLLOWERS = UserAttributes.NUM_OF_FOLLOWERS.getDb();

    // subthread attribs

    // collections
    final static String THREAD_COLLECTION_NAME = SubThreadCommand.THREAD_COLLECTION_NAME;
    final static String USER_COLLECTION_NAME = SubThreadCommand.USER_COLLECTION_NAME;
    final static String SUBTHREAD_COLLECTION_NAME = SubThreadCommand.SUBTHREAD_COLLECTION_NAME;

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
//        if (!arango.collectionExists(DB_NAME, collectionName)) {
//            arango.createCollection(DB_NAME, collectionName, false);
//        }

        arango.createDocument(SubThreadCommand.DB_Name, collectionName, document);
    }

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            assertTrue(arango.isConnected());
            arango.createDatabase(SubThreadCommand.DB_Name);

            arango.createCollection(DB_NAME, USER_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, THREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, SUBTHREAD_COLLECTION_NAME, false);

            moe = new BaseDocument();
            moe.setKey(moeId);
            moe.addAttribute(USR_IS_DELETED, false);
            moe.addAttribute(USR_NUM_OF_FOLLOWERS, 0);

            addObjectToCollection(moe, USER_COLLECTION_NAME);

            manta = new BaseDocument();
            manta.setKey(mantaId);
            manta.addAttribute(USR_IS_DELETED, false);
            manta.addAttribute(USR_NUM_OF_FOLLOWERS, 0);

            addObjectToCollection(manta, SubThreadCommand.USER_COLLECTION_NAME);

            lujine = new BaseDocument();
            lujine.setKey(lujineId);
            lujine.addAttribute(USR_IS_DELETED, false);
            lujine.addAttribute(USR_NUM_OF_FOLLOWERS, 0);

            addObjectToCollection(lujine, SubThreadCommand.USER_COLLECTION_NAME);

            fishThread  = new BaseDocument();

            fishThread.setKey(fishName);
            fishThread.addAttribute(THREAD_CREATOR_ID_DB, mantaId);
            fishThread.addAttribute(THREAD_NUM_OF_FOLLOWERS_DB, 0);
            fishThread.addAttribute(THREAD_DESCRIPTION_DB, "I love Asmak El Rayes 7amido");
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            fishThread.addAttribute(THREAD_DATE_CREATED_DB, sqlDate);

            addObjectToCollection(fishThread, THREAD_COLLECTION_NAME);

            iceCreamThread  = new BaseDocument();

            iceCreamThread.setKey(iceCreamName);
            iceCreamThread.addAttribute(THREAD_CREATOR_ID_DB, lujineId);
            iceCreamThread.addAttribute(THREAD_NUM_OF_FOLLOWERS_DB, 0);
            iceCreamThread.addAttribute(THREAD_DESCRIPTION_DB, "I love Gelati Azza");
            sqlDate = new java.sql.Date(System.currentTimeMillis());
            iceCreamThread.addAttribute(THREAD_DATE_CREATED_DB, sqlDate);

            addObjectToCollection(iceCreamThread, THREAD_COLLECTION_NAME);

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    private static JSONObject createSubthread(String creatorId, String parentThreadId, String title, String content, boolean hasImage) {
        JSONObject body = new JSONObject();
        body.put(SubThreadCommand.PARENT_THREAD_ID, parentThreadId);
        body.put(SubThreadCommand.TITLE, title);
        body.put(SubThreadCommand.CONTENT, content);
        body.put(SubThreadCommand.HASIMAGE, Boolean.toString(hasImage));

        JSONObject uriParams = new JSONObject();
        uriParams.put(SubThreadCommand.CREATOR_ID, creatorId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        CreateSubThread createSubThread = new CreateSubThread();
        return new JSONObject(createSubThread.execute(request));
    }

    @Test
    public void T01_UserCreateSubthreadUnderOwnThread() {

        // manta creates subthread under fish
        String title = "first subthread";
        String content = "Fish is Love. Fish is Life.";
        final boolean hasImage = false;
        JSONObject response = createSubthread(mantaId, fishName, title, content, hasImage);

        assertEquals(200, response.getInt("statusCode"));

        JSONObject responseData = response.getJSONObject("data");

        assertEquals(mantaId, responseData.getString(SubThreadAttributes.CREATOR_ID.getHTTP()));
        assertEquals(fishName, responseData.getString(SubThreadAttributes.PARENT_THREAD_ID.getHTTP()));
        assertEquals(title, responseData.getString(SubThreadAttributes.TITLE.getHTTP()));
        assertEquals(content, responseData.getString(SubThreadAttributes.CONTENT.getHTTP()));
        assertEquals(hasImage, responseData.getBoolean(SubThreadAttributes.HAS_IMAGE.getHTTP()));
        assertEquals(0, responseData.getInt(SubThreadAttributes.LIKES.getHTTP()));
        assertEquals(0, responseData.getInt(SubThreadAttributes.DISLIKES.getHTTP()));
        assertNotNull(responseData.get(SubThreadAttributes.DATE_CREATED.getHTTP()));

    }

    @Test
    public void T01_UserCreateSubthreadUnderOthersThread() {

        // manta creates subthread under ice cream
        String title = "second subthread";
        String content = "Gelati Sucks.";
        final boolean hasImage = false;
        JSONObject response = createSubthread(mantaId, iceCreamName, title, content, hasImage);

        assertEquals(200, response.getInt("statusCode"));

        JSONObject responseData = response.getJSONObject("data");

        assertEquals(mantaId, responseData.getString(SubThreadAttributes.CREATOR_ID.getHTTP()));
        assertEquals(iceCreamName, responseData.getString(SubThreadAttributes.PARENT_THREAD_ID.getHTTP()));
        assertEquals(title, responseData.getString(SubThreadAttributes.TITLE.getHTTP()));
        assertEquals(content, responseData.getString(SubThreadAttributes.CONTENT.getHTTP()));
        assertEquals(hasImage, responseData.getBoolean(SubThreadAttributes.HAS_IMAGE.getHTTP()));
        assertEquals(0, responseData.getInt(SubThreadAttributes.LIKES.getHTTP()));
        assertEquals(0, responseData.getInt(SubThreadAttributes.DISLIKES.getHTTP()));
        assertNotNull(responseData.get(SubThreadAttributes.DATE_CREATED.getHTTP()));

    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(SubThreadCommand.DB_Name);
    }

}
