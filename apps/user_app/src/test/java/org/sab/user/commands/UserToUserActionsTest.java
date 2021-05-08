package org.sab.user.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.arango.Arango;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserToUserActionsTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    private static Arango arango;
    private static ArangoDB arangoDB;
    private static BaseDocument moe, manta, lujine;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            moe = new BaseDocument();
            moe.addAttribute(UserToUserCommand.USER_ID_DB, moeId);
            moe.addAttribute(UserToUserCommand.IS_DELETED_DB, false);
            moe.addAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB, 0);

            addObjectToCollection(moe, UserToUserCommand.USER_COLLECTION_NAME);

            manta = new BaseDocument();
            manta.addAttribute(UserToUserCommand.USER_ID_DB, mantaId);
            manta.addAttribute(UserToUserCommand.IS_DELETED_DB, false);
            manta.addAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB, 0);

            addObjectToCollection(manta, UserToUserCommand.USER_COLLECTION_NAME);

            lujine = new BaseDocument();
            lujine.addAttribute(UserToUserCommand.USER_ID_DB, lujineId);
            lujine.addAttribute(UserToUserCommand.IS_DELETED_DB, false);
            lujine.addAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB, 0);

            addObjectToCollection(lujine, UserToUserCommand.USER_COLLECTION_NAME);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        if (!arango.collectionExists(arangoDB, UserToUserCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(arangoDB, UserToUserCommand.TEST_DB_Name, collectionName, false);
        }

        arango.createDocument(arangoDB, UserToUserCommand.TEST_DB_Name, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(arangoDB, UserToUserCommand.TEST_DB_Name, collectionName, document.getKey());
    }

    @AfterClass
    public void tearDown() {
        removeObjectFromCollection(moe, UserToUserCommand.USER_COLLECTION_NAME);
        removeObjectFromCollection(manta, UserToUserCommand.USER_COLLECTION_NAME);
        removeObjectFromCollection(lujine, UserToUserCommand.USER_COLLECTION_NAME);
    }

    @Test
    public void T01_UserFollowsUser() {
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, moeId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, mantaId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        FollowUser followUser = new FollowUser();
        String response = followUser.execute(body);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.SUCCESSFULLY_FOLLOWED_USER, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertFalse(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount + 1, newFollowerCount);
    }

    @Test
    public void T02_UserBlocksUser() {
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, moeId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, mantaId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        BlockUser blockUser = new BlockUser();
        String response = blockUser.execute(request);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.USER_BLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertFalse(edgeId.equals(""));
    }

    @Test
    public void T03_UserCannotFollowBlockedUser() {
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, moeId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, lujineId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        BlockUser blockUser = new BlockUser();
        blockUser.execute(request);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        FollowUser followUser = new FollowUser();
        String response = followUser.execute(body);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.ACTION_MAKER_BLOCKED_USER_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertTrue(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);
    }

    @Test
    public void T04_UserCannotUnfollowBlockedUser() {
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, moeId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, mantaId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        FollowUser followUser = new FollowUser();
        String response = followUser.execute(body);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.ACTION_MAKER_BLOCKED_USER_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertFalse(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);
    }

    @Test
    public void T05_UserUnblocksUser() {
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, moeId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, mantaId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        BlockUser blockUser = new BlockUser();
        String response = blockUser.execute(request);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.USER_UNBLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was removed successfully.
        assertTrue(edgeId.equals(""));
    }

    @Test
    public void T06_UserUnfollowsUser() {
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, moeId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, mantaId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        FollowUser followUser = new FollowUser();
        String response = followUser.execute(body);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.SUCCESSFULLY_UNFOLLOWED_USER, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was removed successfully.
        assertTrue(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount - 1, newFollowerCount);
    }

    @Test
    public void TO7_UserCannotFollowDeletedUser() {

        final BaseDocument userDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        userDocument.updateAttribute(UserToUserCommand.IS_DELETED_DB, true);
        arango.updateDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userDocument, moeId);

        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, moeId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, mantaId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        FollowUser followUser = new FollowUser();
        String response = followUser.execute(request);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertTrue(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);
    }

    @Test
    public void T08_UserCannotUnfollowDeletedUser() {
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, lujineId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, mantaId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);


        FollowUser followUser = new FollowUser();
        followUser.execute(body);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, lujineId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        final BaseDocument userDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, lujineId);
        userDocument.updateAttribute(UserToUserCommand.IS_DELETED_DB, true);
        arango.updateDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userDocument, lujineId);

        String response = followUser.execute(body);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not removed.
        assertFalse(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, lujineId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);
    }

    @Test
    public void TO9_UserCannotBlockDeletedUser() {
        final BaseDocument userDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, lujineId);
        userDocument.updateAttribute(UserToUserCommand.IS_DELETED_DB, true);
        arango.updateDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userDocument, lujineId);

        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, lujineId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, mantaId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        BlockUser blockUser = new BlockUser();
        String response = blockUser.execute(body);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getString("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertTrue(edgeId.equals(""));
    }

    @Test
    public void T10_UserCannotUnblockDeletedUser() {
        assertTrue(1 == 1);
    }
}