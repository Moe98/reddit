package org.sab.user.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

public class GetMyFollowers extends UserToUserCommand {
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

            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, true);
            }
            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollectionInbound(DB_Name, USER_FOLLOWS_USER_COLLECTION_NAME, USER_COLLECTION_NAME+"/"+userId);
            ArrayList<String> arr = new ArrayList<>();
            arr.add(IS_DELETED_DB);
            arr.add(NUM_OF_FOLLOWERS_DB);
            response = arango.parseOutput(cursor, USER_ID_DB, arr);

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
        GetMyFollowers getMyFollowers = new GetMyFollowers();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(USER_ID, "moe");
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(getMyFollowers.execute(request));
    }
}
