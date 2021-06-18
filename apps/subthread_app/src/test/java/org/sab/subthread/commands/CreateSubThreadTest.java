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

import static org.junit.Assert.*;

public class CreateSubThreadTest {
    // thread attribs
    protected static final String THREAD_DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String THREAD_CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String THREAD_NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String THREAD_DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();
    // db attribs
    final static String DB_NAME = SubThreadCommand.DB_Name;
    // user attributes
    final static String USR_IS_DELETED = UserAttributes.IS_DELETED.getArangoDb();
    final static String USR_NUM_OF_FOLLOWERS = UserAttributes.NUM_OF_FOLLOWERS.getArangoDb();
    // collections
    final static String THREAD_COLLECTION_NAME = SubThreadCommand.THREAD_COLLECTION_NAME;
    final static String USER_COLLECTION_NAME = SubThreadCommand.USER_COLLECTION_NAME;
    final static String SUBTHREAD_COLLECTION_NAME = SubThreadCommand.SUBTHREAD_COLLECTION_NAME;
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";

    // subthread attribs
    final private static String fishName = "AsmakElRayes7amido", iceCreamName = "GelatiAzza";
    private static Arango arango;
    private static BaseDocument moe, manta, lujine, fishThread, iceCreamThread;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();

            arango.createDatabase(SubThreadCommand.DB_Name);

            arango.createCollection(DB_NAME, USER_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, THREAD_COLLECTION_NAME, false);
            arango.createCollection(DB_NAME, SUBTHREAD_COLLECTION_NAME, false);

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

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        JSONObject claims = new JSONObject().put(CommentCommand.USERNAME, creatorId);
        AuthParamsHandler.putAuthorizedParams(request, claims);

        CreateSubThread createSubThread = new CreateSubThread();
        return new JSONObject(createSubThread.execute(request));
    }

    @AfterClass
    public static void tearDown() {
        arango.dropDatabase(SubThreadCommand.DB_Name);
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

}
