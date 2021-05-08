package org.sab.user.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class FollowUser extends UserToUserCommand {
    private Arango arango;
    private ArangoDB arangoDB;

    @Override
    protected String execute() {
        final JSONObject response = new JSONObject();
        String responseMessage = "";

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            final String userId = body.getString(USER_ID);
            final String actionMakerId = uriParams.getString(ACTION_MAKER_ID);

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, true);
            }

            if (!arango.documentExists(arangoDB, DB_Name, USER_COLLECTION_NAME, userId)) {
                responseMessage = "User does not exist.";
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            // Get the user to check if they exist or have been deleted.
            final BaseDocument userDocument = arango.readDocument(arangoDB, DB_Name, USER_COLLECTION_NAME, userId);
            final boolean isDeleted = (boolean) userDocument.getAttribute(IS_DELETED_DB);

            if (isDeleted) {
                responseMessage = "User has deleted their account.";
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            final String edgeKey = Arango.getSingleEdgeId(arango, arangoDB, DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + actionMakerId, USER_COLLECTION_NAME + "/" + userId);

            if (edgeKey.length() != 0) {
                responseMessage = "You have unfollowed this User.";
                arango.deleteDocument(arangoDB, DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, edgeKey);

                // --followerCount;
            } else {
                responseMessage = "You are now following this User!";

                final BaseEdgeDocument userFollowsUserEdge = addEdgeFromUserToUser(actionMakerId, userId);
                arango.createEdgeDocument(arangoDB, DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, userFollowsUserEdge);

                // ++followerCount;
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
            response.put("msg", responseMessage);
        }

        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        final Attribute userId = new Attribute(USER_ID, DataType.STRING, true);

        return new Schema(List.of(userId));
    }
}