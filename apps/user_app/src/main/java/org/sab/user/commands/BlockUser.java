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

public class BlockUser extends UserToUserCommand {
    private Arango arango;
    private ArangoDB arangoDB;

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
        String msg = "";
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_BLOCK_USER_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_BLOCK_USER_COLLECTION_NAME, true);
            }

            if (!arango.documentExists(arangoDB, DB_Name, USER_COLLECTION_NAME, userId)) {
                msg = "User does not exist.";
                return Responder.makeErrorResponse(msg, 404).toString();
            }

            // Get the user to check if they exist or have been deleted.
            final BaseDocument userDocument = arango.readDocument(arangoDB, DB_Name, USER_COLLECTION_NAME, userId);
            final boolean isDeleted = (boolean) userDocument.getAttribute(IS_DELETED_DB);

            if (isDeleted) {
                msg = "User has deleted their account.";
                return Responder.makeErrorResponse(msg, 404).toString();
            }

            String blockEdgeId = Arango.getSingleEdgeId(arango, arangoDB, DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + actionMakerId, USER_COLLECTION_NAME + "/" + userId);

            if (blockEdgeId.length() != 0) {
                msg = "You have unblocked this User.";
                arango.deleteDocument(arangoDB, DB_Name, USER_BLOCK_USER_COLLECTION_NAME, blockEdgeId);
            } else {
                msg = "You have blocked this user.";

                final BaseEdgeDocument userBlockUserEdge = addEdgeFromUserToUser(actionMakerId, userId);
                arango.createEdgeDocument(arangoDB, DB_Name, USER_BLOCK_USER_COLLECTION_NAME, userBlockUserEdge);
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }

    public static void main(String[] args) {
        BlockUser lc = new BlockUser();
//        JSONObject request = new JSONObject("{\"body\":{\"commentId\":\"21289\"},\"uriParams\":{\"userId\":\"asdafsda\"},\"methodType\":\"PUT\"}");


        JSONObject body = new JSONObject();
        body.put(USER_ID, "Moe");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "Manta");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(lc.execute(request));
    }
}