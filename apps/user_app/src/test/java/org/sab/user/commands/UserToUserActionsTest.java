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

            arango.createDatabase(arangoDB, UserToUserCommand.TEST_DB_Name);

            moe = new BaseDocument();
            moe.setKey(moeId);
            moe.addAttribute(UserToUserCommand.IS_DELETED_DB, false);
            moe.addAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB, 0);

            addObjectToCollection(moe, UserToUserCommand.USER_COLLECTION_NAME);

            manta = new BaseDocument();
            manta.setKey(mantaId);
            manta.addAttribute(UserToUserCommand.IS_DELETED_DB, false);
            manta.addAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB, 0);

            addObjectToCollection(manta, UserToUserCommand.USER_COLLECTION_NAME);

            lujine = new BaseDocument();
            lujine.setKey(lujineId);
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
    public static void tearDown() {
        removeObjectFromCollection(moe, UserToUserCommand.USER_COLLECTION_NAME);
        removeObjectFromCollection(manta, UserToUserCommand.USER_COLLECTION_NAME);
        removeObjectFromCollection(lujine, UserToUserCommand.USER_COLLECTION_NAME);
        arango.dropDatabase(arangoDB,UserToUserCommand.TEST_DB_Name);
    }

    @Test
    public void T01_UserFollowsUser() {
        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        String response = userFollowUser(mantaId,moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.SUCCESSFULLY_FOLLOWED_USER, ((JSONObject)responseJson.get("data")).getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertFalse(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount + 1, newFollowerCount);

        // removing the effect of the test:
        userUnfollowUser(mantaId,moeId);
    }

    @Test
    public void T02_UserBlocksUser() {
        String response = userBlockUser(mantaId,moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_BLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE, ((JSONObject)responseJson.get("data")).getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertFalse(edgeId.equals(""));

        //removing the test effect
        userUnblockUser(mantaId,moeId);
    }

    @Test
    public void T03_UserCannotFollowBlockedUser() {
        userBlockUser(lujineId,moeId);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        String response = userFollowUser(lujineId,moeId);
        JSONObject responseJson = new JSONObject(response);
        System.out.println(responseJson);
        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.ACTION_MAKER_BLOCKED_USER_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertTrue(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);

        // removing test effect
        userUnblockUser(lujineId,moeId);
    }

    @Test
    public void T04_UserCannotUnfollowBlockedUser() {
        userFollowUser(mantaId,moeId);
        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        userBlockUser(mantaId,moeId);
        String response = userUnfollowUser(mantaId,moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.ACTION_MAKER_BLOCKED_USER_RESPONSE_MESSAGE,responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertFalse(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);

        //removing test effects
        userUnblockUser(mantaId,moeId);
        userUnfollowUser(mantaId,moeId);
    }

    @Test
    public void T05_UserUnblocksUser() {
        userBlockUser(mantaId,moeId);
        String response = userUnblockUser(mantaId,moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_UNBLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE, ((JSONObject)responseJson.get("data")).getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was removed successfully.
        assertTrue(edgeId.equals(""));
    }

    @Test
    public void T06_UserUnfollowsUser() {
        userFollowUser(mantaId,moeId);
        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        String response =userUnfollowUser(mantaId,moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.SUCCESSFULLY_UNFOLLOWED_USER, ((JSONObject)responseJson.get("data")).getString("msg"));

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
        deleteUser(moeId);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        String response = userFollowUser(mantaId,moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE,responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertTrue(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);

        //removing test effects
        undeleteUser(moeId);
    }

    @Test
    public void T08_UserCannotUnfollowDeletedUser() {
        userFollowUser(mantaId,lujineId);

        final BaseDocument oldUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, lujineId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        deleteUser(lujineId);

        String response = userUnfollowUser(mantaId,lujineId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE,responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not removed.
        assertFalse(edgeId.equals(""));

        final BaseDocument newUserDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, lujineId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);

        //removing test effects
        undeleteUser(lujineId);
        userUnfollowUser(mantaId,lujineId);
    }

    @Test
    public void TO9_UserCannotBlockDeletedUser() {
        deleteUser(lujineId);

        String response = userBlockUser(mantaId,lujineId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertTrue(edgeId.equals(""));

        //removing test effect
        undeleteUser(lujineId);
    }

    @Test
    public void T10_UserCannotUnblockDeletedUser() {
        userBlockUser(mantaId,lujineId);

        deleteUser(lujineId);

        String response = userUnblockUser(mantaId,lujineId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        String edgeId = Arango.getSingleEdgeId(arango, arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertFalse(edgeId.equals(""));

        //removing test effects
        undeleteUser(lujineId);
        userUnblockUser(mantaId,lujineId);
    }

    public static void deleteUser(String userId){
        final BaseDocument userDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userId);
        userDocument.updateAttribute(UserToUserCommand.IS_DELETED_DB, true);
        arango.updateDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userDocument, userId);
    }

    public static String userFollowUser(String actionMakerId, String userId){
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, userId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, actionMakerId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        FollowUser followUser = new FollowUser();
        return followUser.execute(request);
    }

    public static String userBlockUser(String actionMaker, String userId){
        JSONObject body = new JSONObject();
        body.put(UserToUserCommand.USER_ID, userId);

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, actionMaker);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        BlockUser blockUser = new BlockUser();
        return blockUser.execute(request);
    }

    public static void undeleteUser(String userId){
        final BaseDocument userDocument = arango.readDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userId);
        userDocument.updateAttribute(UserToUserCommand.IS_DELETED_DB, false);
        arango.updateDocument(arangoDB, UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userDocument, userId);
    }

    public static String userUnfollowUser(String actionMaker, String userId){
        return userFollowUser(actionMaker, userId);
    }

    public static String userUnblockUser(String actionMaker, String userId){
        return userBlockUser(actionMaker, userId);
    }

}