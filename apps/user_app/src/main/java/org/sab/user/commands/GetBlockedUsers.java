package org.sab.user.commands;

import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;

import java.util.ArrayList;
import java.util.List;

public class GetBlockedUsers extends UserToUserCommand {
    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected String execute() {
        Arango arango = null;
        JSONArray response = new JSONArray();

        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            final String userId = uriParams.getString(USER_ID);

            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, true);

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            final BaseDocument userDocument = arango.readDocument(DB_Name, USER_COLLECTION_NAME, userId);
            final boolean isDeleted = Boolean.parseBoolean(String.valueOf(userDocument.getAttribute(IS_DELETED_DB)));

            if (isDeleted) {
                return Responder.makeErrorResponse(USER_DELETED_RESPONSE_MESSAGE, 404).toString();
            }

            ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(DB_Name, USER_BLOCK_USER_COLLECTION_NAME, USER_COLLECTION_NAME+"/"+userId);
            ArrayList<String> arrOfAttributes = new ArrayList<>();
            arrOfAttributes.add(IS_DELETED_DB);
            arrOfAttributes.add(NUM_OF_FOLLOWERS_DB);
            response = arango.parseOutput(cursor, USER_ID_DB, arrOfAttributes);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }finally {
            if (arango != null) {
                arango.disconnect();
            }
        }
        return Responder.makeDataResponse(response).toString();
    }

    public static void main(String[] args) {
        GetBlockedUsers getBlockedUsers = new GetBlockedUsers();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(USER_ID, "manta");
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(getBlockedUsers.execute(request));
    }
}
