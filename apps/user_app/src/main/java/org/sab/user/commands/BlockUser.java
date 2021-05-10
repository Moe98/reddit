package org.sab.user.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class BlockUser extends UserToUserCommand {
    @Override
    protected Schema getSchema() {
        final Attribute userId = new Attribute(USER_ID, DataType.STRING, true);
        return new Schema(List.of(userId));
    }

    @Override
    protected String execute() {
        String actionMakerId = uriParams.getString(ACTION_MAKER_ID);
        String userId = body.getString(USER_ID);

        JSONObject response = new JSONObject();
        String responseMessage = "";
        try {
            Arango arango = Arango.getInstance();

//            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_BLOCK_USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, true);
            }

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                responseMessage = USER_DOES_NOT_EXIST_RESPONSE_MESSAGE;
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            String actionMakerBlockedEdge = Arango.getSingleEdgeId(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, USER_COLLECTION_NAME + "/" + actionMakerId);
            if (actionMakerBlockedEdge.length() != 0) {
                responseMessage = USER_BLOCKED_ACTION_MAKER_RESPONSE_MESSAGE;
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            // Get the user to check if they exist or have been deleted.
            final BaseDocument userDocument = arango.readDocument(DB_Name, USER_COLLECTION_NAME, userId);
            final boolean isDeleted = (boolean) userDocument.getAttribute(IS_DELETED_DB);

            if (isDeleted) {
                responseMessage = USER_DELETED_RESPONSE_MESSAGE;
                return Responder.makeErrorResponse(responseMessage, 404).toString();
            }

            String blockEdgeId = Arango.getSingleEdgeId(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + actionMakerId, USER_COLLECTION_NAME + "/" + userId);

            if (blockEdgeId.length() != 0) {
                responseMessage = USER_UNBLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE;
                arango.deleteDocument(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, blockEdgeId);
            } else {
                responseMessage = USER_BLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE;

                final BaseEdgeDocument userBlockUserEdge = addEdgeFromUserToUser(actionMakerId, userId);
                arango.createEdgeDocument(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, userBlockUserEdge);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            response.put("msg", responseMessage);
        }
        return Responder.makeDataResponse(response).toString();
    }
}