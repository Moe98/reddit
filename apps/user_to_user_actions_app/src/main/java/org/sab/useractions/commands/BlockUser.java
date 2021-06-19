package org.sab.useractions.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.NotificationMessages;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.notifyApp;

public class BlockUser extends UserToUserCommand {
    @Override
    protected Schema getSchema() {
        final Attribute userId = new Attribute(USER_ID, DataType.STRING, true);
        return new Schema(List.of(userId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {
        Arango arango = null;

        JSONObject response = new JSONObject();
        String responseMessage = "";
        try {
            arango = Arango.getInstance();

            String actionMakerId = uriParams.getString(ACTION_MAKER_ID);
            String userId = body.getString(USER_ID);

            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);

            arango.createCollectionIfNotExists(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, true);

            arango.createCollectionIfNotExists(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, true);

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                responseMessage = USER_DOES_NOT_EXIST_RESPONSE_MESSAGE;
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            String actionMakerBlockedEdge = arango.getSingleEdgeId(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, USER_COLLECTION_NAME + "/" + actionMakerId);
            if (actionMakerBlockedEdge.length() != 0) {
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

            String blockEdgeId = arango.getSingleEdgeId(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + actionMakerId, USER_COLLECTION_NAME + "/" + userId);


            if (blockEdgeId.length() != 0) {// the user already blocks said user
                responseMessage = USER_UNBLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE;
                arango.deleteDocument(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, blockEdgeId);


            } else {
                responseMessage = USER_BLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE;

                final BaseEdgeDocument userBlockUserEdge = addEdgeFromUserToUser(actionMakerId, userId);
                arango.createEdgeDocument(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, userBlockUserEdge);

                // TODO: make sure this is working, it should be as it's copied from the working & tested command "followUser"
                final String edgeKey = arango.getSingleEdgeId(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + actionMakerId, USER_COLLECTION_NAME + "/" + userId);

                if(edgeKey.length() != 0){// if the user was following the user who he just blocked now, then unfollow said user
                    int followerCount = Integer.parseInt(String.valueOf(userDocument.getAttribute(NUM_OF_FOLLOWERS_DB)));
                    arango.deleteDocument(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, edgeKey);
                    --followerCount;
                    userDocument.updateAttribute(NUM_OF_FOLLOWERS_DB, followerCount);
                    arango.updateDocument(DB_Name, USER_COLLECTION_NAME, userDocument, userId);

                }

            }

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            response.put("msg", responseMessage);
        }

        return Responder.makeDataResponse(response).toString();
    }
}