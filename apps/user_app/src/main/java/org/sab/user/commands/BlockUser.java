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
            arango.connectIfNotConnected();

            String actionMakerId = uriParams.getString(ACTION_MAKER_ID);
            String userId = body.getString(USER_ID);

//            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);

            arango.createCollectionIfNotExists(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, true);

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

            if (blockEdgeId.length() != 0) {
                responseMessage = USER_UNBLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE;
                arango.deleteDocument(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, blockEdgeId);
            } else {
                responseMessage = USER_BLOCKED_SUCCESSFULLY_RESPONSE_MESSAGE;

                final BaseEdgeDocument userBlockUserEdge = addEdgeFromUserToUser(actionMakerId, userId);
                arango.createEdgeDocument(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, userBlockUserEdge);
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
            response.put("msg", responseMessage);
        }
        return Responder.makeDataResponse(response).toString();
    }
}