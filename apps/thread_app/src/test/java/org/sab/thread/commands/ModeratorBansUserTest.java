package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.models.ThreadAttributes;
import org.sab.models.user.UserAttributes;

import static org.junit.Assert.*;

public class ModeratorBansUserTest {
    private static final String DB_NAME = System.getenv("ARANGO_DB");
    private static final String threadId = "TestThread", moderatorId = "Moderator", bannedUserId = "BannedUser";
    private static Arango arango;
    private static BaseDocument moderator, bannedUser;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            arango.createDatabaseIfNotExists(DB_NAME);

            arango.createCollection(DB_NAME, ThreadCommand.THREAD_COLLECTION_NAME, false);

            moderator = new BaseDocument();
            moderator.setKey(moderatorId);
            moderator.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
            moderator.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
            addObjectToCollection(moderator, "User");

            bannedUser = new BaseDocument();
            bannedUser.setKey(bannedUserId);
            bannedUser.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
            bannedUser.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
            addObjectToCollection(bannedUser, "User");

            // TODO should be DB insert
//            BaseDocument thread = TestUtils.setUpThread(threadId, moderatorId, 0, "description");
//            TestUtils.addObjectToCollection(arango, thread, ThreadCommand.THREAD_COLLECTION_NAME);
            JSONObject request = new JSONObject();

            JSONObject body = new JSONObject();
            body.put(ThreadAttributes.THREAD_NAME.getHTTP(), threadId);
            body.put(ThreadAttributes.DESCRIPTION.getHTTP(), "description");

            JSONObject uriParams = new JSONObject();
            uriParams.put(ThreadAttributes.CREATOR_ID.getHTTP(), moderatorId);

            request.put("body", body);
            request.put("uriParams", uriParams);
            request.put("methodType", "POST");

            final CreateThread createThread = new CreateThread();
            createThread.execute(request);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        removeObjectFromCollection(moderator, "User");
        removeObjectFromCollection(bannedUser, "User");
        arango.dropDatabase(DB_NAME);
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        arango.createCollectionIfNotExists(DB_NAME, collectionName, false);
        arango.createDocument(DB_NAME, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(DB_NAME, collectionName, document.getKey());
    }

    private static JSONObject moderatorBansUserFromThread(String moderatorId, String bannedUserId, String threadName) {
        JSONObject body = new JSONObject();
        body.put(ThreadAttributes.THREAD_NAME.getHTTP(), threadName);
        body.put(ThreadAttributes.BANNED_USER_ID.getHTTP(), bannedUserId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(ThreadAttributes.ACTION_MAKER_ID.getHTTP(), moderatorId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        ModeratorBansUser moderatorBansUser = new ModeratorBansUser();

        return new JSONObject(moderatorBansUser.execute(request));
    }

    private static void removeBanFromUserInThread(String userId, String threadId) {
        final String bannedUserEdgeKey = arango.getSingleEdgeId(DB_NAME,
                ThreadCommand.USER_BANNED_FROM_THREAD_COLLECTION_NAME,
                ThreadCommand.USER_COLLECTION_NAME + "/" + userId,
                ThreadCommand.THREAD_COLLECTION_NAME + "/" + threadId);

        arango.deleteDocument(DB_NAME, ThreadCommand.USER_BANNED_FROM_THREAD_COLLECTION_NAME, bannedUserEdgeKey);
    }

    private boolean edgeExistsFromUserToThread(String edgeCollectionName, String userId, String threadId) {
        final String edgeId = arango.getSingleEdgeId(DB_NAME, edgeCollectionName, ThreadCommand.USER_COLLECTION_NAME + "/" + userId, ThreadCommand.THREAD_COLLECTION_NAME + "/" + threadId);

        return !edgeId.equals("");
    }

    @Test
    public void moderatorBansUser() {
        final JSONObject response = moderatorBansUserFromThread(moderatorId, bannedUserId, threadId);

        assertEquals(200, response.getInt("statusCode"));
        assertEquals(ThreadCommand.USER_BANNED_SUCCESSFULLY, ((JSONObject) response.get("data")).getString("msg"));

        assertTrue(edgeExistsFromUserToThread(ThreadCommand.USER_BANNED_FROM_THREAD_COLLECTION_NAME, bannedUserId, threadId));

        removeBanFromUserInThread(bannedUserId, threadId);
    }

    @Test
    public void cannotBanUserTwice() {
        JSONObject response = moderatorBansUserFromThread(moderatorId, bannedUserId, threadId);

        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));

        response = moderatorBansUserFromThread(moderatorId, bannedUserId, threadId);

        assertEquals(400, response.getInt("statusCode"));
        assertEquals(ThreadCommand.USER_ALREADY_BANNED, response.get("msg"));

        assertTrue(edgeExistsFromUserToThread(ThreadCommand.USER_BANNED_FROM_THREAD_COLLECTION_NAME, bannedUserId, threadId));

        removeBanFromUserInThread(bannedUserId, threadId);
    }

    @Test
    public void onlyModeratorsCanBanUsers() {
        final String dummyUserId = "DummyUser";
        final BaseDocument dummyUserDocument = new BaseDocument();
        dummyUserDocument.setKey(dummyUserId);
        dummyUserDocument.addAttribute(UserAttributes.IS_DELETED.getArangoDb(), false);
        dummyUserDocument.addAttribute(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);
        addObjectToCollection(dummyUserDocument, "User");

        final JSONObject response = moderatorBansUserFromThread(dummyUserId, bannedUserId, threadId);

        System.out.println(response);
        assertEquals(401, response.getInt("statusCode"));
        assertEquals(ThreadCommand.NOT_A_MODERATOR, response.get("msg"));

        assertFalse(edgeExistsFromUserToThread(ThreadCommand.USER_BANNED_FROM_THREAD_COLLECTION_NAME, bannedUserId, threadId));
    }
}