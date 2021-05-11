package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

public class GetMyDislikedSubThreads extends SubThreadCommand{
    @Override
    protected String execute() {
        JSONArray response = new JSONArray();
        final String userId = uriParams.getString(USER_ID);
        try {
            final Arango arango = Arango.getInstance();

            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, true);
            }

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }
            ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, USER_COLLECTION_NAME+"/"+userId);
            ArrayList<String> arr = new ArrayList<>();
            arr.add(PARENT_THREAD_ID_DB);
            arr.add(CREATOR_ID_DB);
            arr.add(CONTENT_DB);
            arr.add(TITLE_DB);
            arr.add(HASIMAGE_DB);
            arr.add(LIKES_DB);
            arr.add(DISLIKES_DB);
            arr.add(DATE_CREATED_DB);
            response = arango.parseOutput(cursor, SUBTHREAD_ID_DB, arr);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }
        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }

    public static void main(String[] args) {
        GetMyLikedSubThreads getMyLikedSubThreads = new GetMyLikedSubThreads();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(USER_ID, "manta");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(getMyLikedSubThreads.execute(request));

    }
}
