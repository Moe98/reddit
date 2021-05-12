package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GetSubThreads extends SubThreadCommand {
    @Override
    protected String execute() {
        Arango arango = null;
        JSONArray response = new JSONArray();
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            final String threadId = uriParams.getString(THREAD_ID);

            if (!arango.collectionExists(DB_Name, THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, THREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            }

            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            ArangoCursor<BaseDocument> cursor = arango.filterCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, PARENT_THREAD_ID_DB, threadId);
            ArrayList<String> arr = new ArrayList<>();
            arr.add(PARENT_THREAD_ID_DB);
            arr.add(CREATOR_ID_DB);
            arr.add(TITLE_DB);
            arr.add(CONTENT_DB);
            arr.add(LIKES_DB);
            arr.add(DISLIKES_DB);
            response = arango.parseOutput(cursor, SUBTHREAD_ID_DB, arr);

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
        GetSubThreads getSubthreads = new GetSubThreads();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(THREAD_ID, "asmakElRayes7amido");
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(getSubthreads.execute(request));
    }
}
