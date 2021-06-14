package org.sab.useractions.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;

import static org.junit.Assert.*;

public class UserToUserActionsTest {
    final private static String moeId = "Moe", mantaId = "Manta", lujineId = "Lujine";
    private static Arango arango;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            assertTrue(arango.isConnected());
            arango.createDatabaseIfNotExists(UserToUserCommand.TEST_DB_Name);

            BaseDocument moe = new BaseDocument();
            moe.setKey(moeId);
            moe.addAttribute(UserToUserCommand.IS_DELETED_DB, false);
            moe.addAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB, 0);

            addObjectToCollection(moe, UserToUserCommand.USER_COLLECTION_NAME);

            BaseDocument manta = new BaseDocument();
            manta.setKey(mantaId);
            manta.addAttribute(UserToUserCommand.IS_DELETED_DB, false);
            manta.addAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB, 0);

            addObjectToCollection(manta, UserToUserCommand.USER_COLLECTION_NAME);

            BaseDocument lujine = new BaseDocument();
            lujine.setKey(lujineId);
            lujine.addAttribute(UserToUserCommand.IS_DELETED_DB, false);
            lujine.addAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB, 0);

            addObjectToCollection(lujine, UserToUserCommand.USER_COLLECTION_NAME);
        } catch (Exception e) {
            System.out.println("failed");
            fail(e.getMessage());
        }
    }

    private static void addObjectToCollection(BaseDocument document, String collectionName) {
        // TODO: Add testing DB.
        if (!arango.collectionExists(UserToUserCommand.TEST_DB_Name, collectionName)) {
            arango.createCollection(UserToUserCommand.TEST_DB_Name, collectionName, false);
        }

        arango.createDocument(UserToUserCommand.TEST_DB_Name, collectionName, document);
    }

    private static void removeObjectFromCollection(BaseDocument document, String collectionName) {
        arango.deleteDocument(UserToUserCommand.TEST_DB_Name, collectionName, document.getKey());
    }

    @AfterClass
    public static void tearDown() {
        arango.connectIfNotConnected();
        arango.dropDatabase(UserToUserCommand.TEST_DB_Name);
    }

    public static void deleteUser(String userId) {
        arango.connectIfNotConnected();
        final BaseDocument userDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userId);
        userDocument.updateAttribute(UserToUserCommand.IS_DELETED_DB, true);
        arango.connectIfNotConnected();
        arango.updateDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userDocument, userId);
    }

    public static String userFollowUser(String actionMakerId, String userId) {
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

    public static String userBlockUser(String actionMaker, String userId) {
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

    public static void undeleteUser(String userId) {
        arango.connectIfNotConnected();
        final BaseDocument userDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userId);
        userDocument.updateAttribute(UserToUserCommand.IS_DELETED_DB, false);
        arango.connectIfNotConnected();
        arango.updateDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, userDocument, userId);
    }

    public static String userUnfollowUser(String actionMaker, String userId) {
        return userFollowUser(actionMaker, userId);
    }

    public static String userUnblockUser(String actionMaker, String userId) {
        return userBlockUser(actionMaker, userId);
    }

    public static String getBlockedUsers(String actionMaker) {
        GetBlockedUsers getBlockedUsers = new GetBlockedUsers();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, actionMaker);
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        return getBlockedUsers.execute(request);
    }

    public static String getFollowedUsers(String actionMaker) {
        GetFollowedUsers getFollowedUsers = new GetFollowedUsers();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, actionMaker);
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        return getFollowedUsers.execute(request);
    }

    public static String getMyFollowers(String actionMaker) {
        GetMyFollowers getMyFollowers = new GetMyFollowers();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(UserToUserCommand.ACTION_MAKER_ID, actionMaker);
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        return getMyFollowers.execute(request);
    }

    @Test
    public void T01_UserFollowsUser() {
        arango.connectIfNotConnected();
        final BaseDocument oldUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));
        arango.connectIfNotConnected();
        String response = userFollowUser(mantaId, moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.SUCCESSFULLY_FOLLOWED_USER, ((JSONObject) responseJson.get("data")).getString("msg"));
        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertNotEquals("", edgeId);
        arango.connectIfNotConnected();
        final BaseDocument newUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount + 1, newFollowerCount);

        // removing the effect of the test:
        arango.connectIfNotConnected();
        userUnfollowUser(mantaId, moeId);
    }

    @Test
    public void T02_UserBlocksUser() {
        arango.connectIfNotConnected();
        String response = userBlockUser(mantaId, moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_BLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE, ((JSONObject) responseJson.get("data")).getString("msg"));
        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertNotEquals("", edgeId);

        //removing the test effect
        arango.connectIfNotConnected();
        userUnblockUser(mantaId, moeId);
    }

    @Test
    public void T03_UserCannotFollowBlockedUser() {
        arango.connectIfNotConnected();
        userBlockUser(lujineId, moeId);

        arango.connectIfNotConnected();
        final BaseDocument oldUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        arango.connectIfNotConnected();
        String response = userFollowUser(lujineId, moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.ACTION_MAKER_BLOCKED_USER_RESPONSE_MESSAGE, responseJson.getString("msg"));

        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertEquals("", edgeId);

        arango.connectIfNotConnected();
        final BaseDocument newUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);

        // removing test effect
        arango.connectIfNotConnected();
        userUnblockUser(lujineId, moeId);
    }

    //TODO: blocking now removes the follow link, so this test isn't needed, we can remove it.
    @Test
    public void T04_UserCannotUnfollowBlockedUser() {
        arango.connectIfNotConnected();
        userFollowUser(mantaId, moeId);

        arango.connectIfNotConnected();
        final BaseDocument oldUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        arango.connectIfNotConnected();
        userBlockUser(mantaId, moeId);

        arango.connectIfNotConnected();
        String response = userUnfollowUser(mantaId, moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.ACTION_MAKER_BLOCKED_USER_RESPONSE_MESSAGE, responseJson.getString("msg"));

        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was added successfully.
        assertEquals("", edgeId);

        arango.connectIfNotConnected();
        final BaseDocument newUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount+1);

        //removing test effects
        arango.connectIfNotConnected();
        userUnblockUser(mantaId, moeId);
        arango.connectIfNotConnected();
        userUnfollowUser(mantaId, moeId);
    }

    @Test
    public void T05_UserUnblocksUser() {
        arango.connectIfNotConnected();
        userBlockUser(mantaId, moeId);

        String response = userUnblockUser(mantaId, moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_UNBLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE, ((JSONObject) responseJson.get("data")).getString("msg"));

        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means that it was removed successfully.
        assertEquals("", edgeId);
    }

    @Test
    public void T06_UserUnfollowsUser() {
        arango.connectIfNotConnected();
        userFollowUser(mantaId, moeId);

        arango.connectIfNotConnected();
        final BaseDocument oldUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        arango.connectIfNotConnected();
        String response = userUnfollowUser(mantaId, moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.SUCCESSFULLY_UNFOLLOWED_USER, ((JSONObject) responseJson.get("data")).getString("msg"));

        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was removed successfully.
        assertEquals("", edgeId);

        arango.connectIfNotConnected();
        final BaseDocument newUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount - 1, newFollowerCount);
    }

    @Test
    public void TO7_UserCannotFollowDeletedUser() {
        arango.connectIfNotConnected();
        deleteUser(moeId);

        arango.connectIfNotConnected();
        final BaseDocument oldUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        arango.connectIfNotConnected();
        String response = userFollowUser(mantaId, moeId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + moeId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertEquals("", edgeId);

        arango.connectIfNotConnected();
        final BaseDocument newUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, moeId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);

        //removing test effects
        arango.connectIfNotConnected();
        undeleteUser(moeId);
    }

    @Test
    public void T08_UserCannotUnfollowDeletedUser() {
        arango.connectIfNotConnected();
        userFollowUser(mantaId, lujineId);

        arango.connectIfNotConnected();
        final BaseDocument oldUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, lujineId);
        int oldFollowerCount = Integer.parseInt(String.valueOf(oldUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        arango.connectIfNotConnected();
        deleteUser(lujineId);

        arango.connectIfNotConnected();
        String response = userUnfollowUser(mantaId, lujineId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_FOLLOWS_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not removed.
        assertNotEquals("", edgeId);

        arango.connectIfNotConnected();
        final BaseDocument newUserDocument = arango.readDocument(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_COLLECTION_NAME, lujineId);
        int newFollowerCount = Integer.parseInt(String.valueOf(newUserDocument.getAttribute(UserToUserCommand.NUM_OF_FOLLOWERS_DB)));

        assertEquals(oldFollowerCount, newFollowerCount);

        //removing test effects
        arango.connectIfNotConnected();
        undeleteUser(lujineId);
        arango.connectIfNotConnected();
        userUnfollowUser(mantaId, lujineId);
    }

    @Test
    public void TO9_UserCannotBlockDeletedUser() {
        arango.connectIfNotConnected();
        deleteUser(lujineId);

        arango.connectIfNotConnected();
        String response = userBlockUser(mantaId, lujineId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertEquals("", edgeId);

        //removing test effect
        arango.connectIfNotConnected();
        undeleteUser(lujineId);
    }

    @Test
    public void T10_UserCannotUnblockDeletedUser() {
        arango.connectIfNotConnected();
        userBlockUser(mantaId, lujineId);

        arango.connectIfNotConnected();
        deleteUser(lujineId);

        arango.connectIfNotConnected();
        String response = userUnblockUser(mantaId, lujineId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        arango.connectIfNotConnected();
        String edgeId = arango.getSingleEdgeId(UserToUserCommand.TEST_DB_Name, UserToUserCommand.USER_BLOCK_USER_COLLECTION_NAME, UserToUserCommand.USER_COLLECTION_NAME + "/" + mantaId, UserToUserCommand.USER_COLLECTION_NAME + "/" + lujineId);

        // If the length of |edgeId| is equal to zero, then that edge does not exist.
        // This means it was not added.
        assertNotEquals("", edgeId);

        //removing test effects
        arango.connectIfNotConnected();
        undeleteUser(lujineId);
        arango.connectIfNotConnected();
        userUnblockUser(mantaId, lujineId);
    }

    @Test
    public void T11_GetBlockedUsers() {
        arango.connectIfNotConnected();
        userBlockUser(mantaId, lujineId);

        arango.connectIfNotConnected();
        userBlockUser(mantaId, moeId);

        arango.connectIfNotConnected();
        userBlockUser(lujineId, moeId);

        arango.connectIfNotConnected();
        String response = getBlockedUsers(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));

        assertEquals(2, dataArr.length());
        boolean scenario1 = lujineId.equals(((JSONObject) dataArr.get(0)).getString("UserId")) & moeId.equals(((JSONObject) dataArr.get(1)).getString("UserId"));
        boolean scenario2 = moeId.equals(((JSONObject) dataArr.get(0)).getString("UserId")) & lujineId.equals(((JSONObject) dataArr.get(1)).getString("UserId"));
        assertTrue(scenario1 || scenario2);

        arango.connectIfNotConnected();
        String response2 = getBlockedUsers(lujineId);
        JSONObject responseJson2 = new JSONObject(response2);

        assertEquals(200, responseJson2.getInt("statusCode"));
        JSONArray dataArr2 = (JSONArray) (responseJson2.get("data"));
        assertEquals(1, dataArr2.length());
        assertEquals(moeId, ((JSONObject) dataArr2.get(0)).getString("UserId"));

        arango.connectIfNotConnected();
        String response3 = getBlockedUsers(moeId);
        JSONObject responseJson3 = new JSONObject(response3);

        assertEquals(200, responseJson3.getInt("statusCode"));
        JSONArray dataArr3 = (JSONArray) (responseJson3.get("data"));
        assertEquals(0, dataArr3.length());

        //removing test effects
        arango.connectIfNotConnected();
        userUnblockUser(mantaId, lujineId);
        arango.connectIfNotConnected();
        userUnblockUser(mantaId, moeId);
        arango.connectIfNotConnected();
        userUnblockUser(lujineId, moeId);
    }

    @Test
    public void T12_GetFollowedUsers() {
        arango.connectIfNotConnected();
        userFollowUser(mantaId, lujineId);

        arango.connectIfNotConnected();
        userFollowUser(mantaId, moeId);

        arango.connectIfNotConnected();
        userFollowUser(lujineId, moeId);

        arango.connectIfNotConnected();
        String response = getFollowedUsers(mantaId);
        JSONObject responseJson = new JSONObject(response);
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));

        assertEquals(2, dataArr.length());
        boolean scenario1 = lujineId.equals(((JSONObject) dataArr.get(0)).getString("UserId")) & moeId.equals(((JSONObject) dataArr.get(1)).getString("UserId"));
        boolean scenario2 = moeId.equals(((JSONObject) dataArr.get(0)).getString("UserId")) & lujineId.equals(((JSONObject) dataArr.get(1)).getString("UserId"));
        assertTrue(scenario1 || scenario2);

        arango.connectIfNotConnected();
        String response2 = getFollowedUsers(lujineId);
        JSONObject responseJson2 = new JSONObject(response2);

        assertEquals(200, responseJson2.getInt("statusCode"));
        JSONArray dataArr2 = (JSONArray) (responseJson2.get("data"));
        assertEquals(1, dataArr2.length());
        assertEquals(moeId, ((JSONObject) dataArr2.get(0)).getString("UserId"));

        arango.connectIfNotConnected();
        String response3 = getFollowedUsers(moeId);
        JSONObject responseJson3 = new JSONObject(response3);

        assertEquals(200, responseJson3.getInt("statusCode"));
        JSONArray dataArr3 = (JSONArray) (responseJson3.get("data"));
        assertEquals(0, dataArr3.length());

        //removing test effects
        arango.connectIfNotConnected();
        userUnfollowUser(mantaId, lujineId);
        arango.connectIfNotConnected();
        userUnfollowUser(mantaId, moeId);
        arango.connectIfNotConnected();
        userUnfollowUser(lujineId, moeId);
    }

    @Test
    public void T13_GetMyFollowers() {
        arango.connectIfNotConnected();
        userFollowUser(lujineId, mantaId);

        arango.connectIfNotConnected();
        userFollowUser(moeId, mantaId);

        arango.connectIfNotConnected();
        userFollowUser(moeId, lujineId);

        arango.connectIfNotConnected();
        String response = getMyFollowers(mantaId);
        JSONObject responseJson = new JSONObject(response);
        assertEquals(200, responseJson.getInt("statusCode"));
        JSONArray dataArr = (JSONArray) (responseJson.get("data"));

        assertEquals(2, dataArr.length());
        boolean scenario1 = lujineId.equals(((JSONObject) dataArr.get(0)).getString("UserId")) & moeId.equals(((JSONObject) dataArr.get(1)).getString("UserId"));
        boolean scenario2 = moeId.equals(((JSONObject) dataArr.get(0)).getString("UserId")) & lujineId.equals(((JSONObject) dataArr.get(1)).getString("UserId"));
        assertTrue(scenario1 || scenario2);

        arango.connectIfNotConnected();
        String response2 = getMyFollowers(lujineId);
        JSONObject responseJson2 = new JSONObject(response2);

        assertEquals(200, responseJson2.getInt("statusCode"));
        JSONArray dataArr2 = (JSONArray) (responseJson2.get("data"));
        assertEquals(1, dataArr2.length());
        assertEquals(moeId, ((JSONObject) dataArr2.get(0)).getString("UserId"));

        arango.connectIfNotConnected();
        String response3 = getMyFollowers(moeId);
        JSONObject responseJson3 = new JSONObject(response3);

        assertEquals(200, responseJson3.getInt("statusCode"));
        JSONArray dataArr3 = (JSONArray) (responseJson3.get("data"));
        assertEquals(0, dataArr3.length());

        //removing test effects
        arango.connectIfNotConnected();
        userUnfollowUser(lujineId, mantaId);
        arango.connectIfNotConnected();
        userUnfollowUser(moeId, mantaId);
        arango.connectIfNotConnected();
        userUnfollowUser(moeId, lujineId);
    }

    @Test
    public void T14_DeletedUserCantGetBlockedUsers() {
        arango.connectIfNotConnected();
        userBlockUser(mantaId, lujineId);

        arango.connectIfNotConnected();
        userBlockUser(mantaId, moeId);

        arango.connectIfNotConnected();
        deleteUser(mantaId);

        arango.connectIfNotConnected();
        String response = getBlockedUsers(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        //removing test effects
        arango.connectIfNotConnected();
        undeleteUser(mantaId);
        arango.connectIfNotConnected();
        userUnblockUser(mantaId, lujineId);
        arango.connectIfNotConnected();
        userUnblockUser(mantaId, moeId);
    }

    @Test
    public void T15_DeletedUserCantGetFollowedUsers() {
        arango.connectIfNotConnected();
        userFollowUser(mantaId, lujineId);

        arango.connectIfNotConnected();
        userFollowUser(mantaId, moeId);

        arango.connectIfNotConnected();
        deleteUser(mantaId);

        arango.connectIfNotConnected();
        String response = getFollowedUsers(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        //removing test effects
        arango.connectIfNotConnected();
        undeleteUser(mantaId);
        arango.connectIfNotConnected();
        userUnfollowUser(mantaId, lujineId);
        arango.connectIfNotConnected();
        userUnfollowUser(mantaId, moeId);
    }

    @Test
    public void T15_DeletedUserCantGetTheirFollowers() {
        arango.connectIfNotConnected();
        userFollowUser(lujineId, mantaId);

        arango.connectIfNotConnected();
        userFollowUser(moeId, mantaId);

        arango.connectIfNotConnected();
        deleteUser(mantaId);

        arango.connectIfNotConnected();
        String response = getMyFollowers(mantaId);
        JSONObject responseJson = new JSONObject(response);

        assertEquals(404, responseJson.getInt("statusCode"));
        assertEquals(UserToUserCommand.USER_DELETED_RESPONSE_MESSAGE, responseJson.getString("msg"));

        //removing test effects
        arango.connectIfNotConnected();
        undeleteUser(mantaId);
        arango.connectIfNotConnected();
        userUnfollowUser(lujineId, mantaId);
        arango.connectIfNotConnected();
        userUnfollowUser(moeId, mantaId);
    }
}
