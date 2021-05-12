package org.sab.user.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class FollowUser extends UserToUserCommand {
    @Override
    protected String execute() {
        Arango arango = null;
        final JSONObject response = new JSONObject();
        String responseMessage = "";

        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            final String userId = body.getString(USER_ID);
            final String actionMakerId = uriParams.getString(ACTION_MAKER_ID);

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }

            if (!arango.collectionExists(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, true);
            }

            if (!arango.collectionExists(DB_Name, USER_BLOCK_USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, true);
            }

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                responseMessage = USER_DOES_NOT_EXIST_RESPONSE_MESSAGE;
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            final String actionMakerBlockedUser = arango.getSingleEdgeId(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + actionMakerId, USER_COLLECTION_NAME + "/" + userId);

            // TODO: If user can unfollow a user they blocked, then delete this condition.
            if (actionMakerBlockedUser.length() != 0) {
                responseMessage = ACTION_MAKER_BLOCKED_USER_RESPONSE_MESSAGE;
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            final String userBlockedActionMaker = arango.getSingleEdgeId(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, USER_COLLECTION_NAME + "/" + actionMakerId);

            if (userBlockedActionMaker.length() != 0) {
                responseMessage = USER_BLOCKED_ACTION_MAKER_RESPONSE_MESSAGE;
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            // Get the user to check if they exist or have been deleted.
            final BaseDocument userDocument = arango.readDocument(DB_Name, USER_COLLECTION_NAME, userId);
            final boolean isDeleted = Boolean.parseBoolean(String.valueOf(userDocument.getAttribute(IS_DELETED_DB)));

            if (isDeleted) {
                responseMessage = USER_DELETED_RESPONSE_MESSAGE;
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            final String edgeKey = arango.getSingleEdgeId(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + actionMakerId, USER_COLLECTION_NAME + "/" + userId);
            int followerCount = Integer.parseInt(String.valueOf(userDocument.getAttribute(NUM_OF_FOLLOWERS_DB)));

            if (edgeKey.length() != 0) {
                responseMessage = SUCCESSFULLY_UNFOLLOWED_USER;
                arango.deleteDocument(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, edgeKey);
                --followerCount;

            } else {
                responseMessage = SUCCESSFULLY_FOLLOWED_USER;

                final BaseEdgeDocument userFollowsUserEdge = addEdgeFromUserToUser(actionMakerId, userId);
                arango.createEdgeDocument(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, userFollowsUserEdge);
                ++followerCount;
            }

            userDocument.updateAttribute(NUM_OF_FOLLOWERS_DB, followerCount);
            arango.updateDocument(DB_Name, USER_COLLECTION_NAME, userDocument, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
            response.put("msg", responseMessage);
        }

        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        final Attribute userId = new Attribute(USER_ID, DataType.STRING, true);

        return new Schema(List.of(userId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    public static void main(String[] args) {
        FollowUser followUser = new FollowUser();
        JSONObject body = new JSONObject();
        body.put(USER_ID, "moe");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "manta");
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(followUser.execute(request));
    }
}