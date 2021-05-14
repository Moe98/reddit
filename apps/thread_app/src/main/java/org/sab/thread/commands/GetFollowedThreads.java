package org.sab.thread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GetFollowedThreads extends ThreadCommand {
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
            if (!arango.collectionExists(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, true);
            }

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }
            ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, USER_COLLECTION_NAME+"/"+userId);
            ArrayList<String> arr = new ArrayList<>();
            arr.add(NUM_OF_FOLLOWERS_DB);
            arr.add(DESCRIPTION_DB);
            arr.add(CREATOR_ID_DB);
            arr.add(DATE_CREATED_DB);
            response = arango.parseOutput(cursor, THREAD_NAME, arr);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }finally {
            if (arango != null) {
                arango.disconnect();
            }
        }
        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }

    public static void main(String[] args) {
        GetFollowedThreads getFollowedThreads = new GetFollowedThreads();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "Manta");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(getFollowedThreads.execute(request));

    }
}
