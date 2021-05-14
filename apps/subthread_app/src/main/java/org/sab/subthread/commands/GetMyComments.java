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
import org.sab.service.validation.HTTPMethod;

public class GetMyComments extends CommentCommand{
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
            if (!arango.collectionExists(DB_Name, COMMENT_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, COMMENT_COLLECTION_NAME, false);
            }

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }
            ArangoCursor<BaseDocument> cursor = arango.filterCollection(DB_Name, COMMENT_COLLECTION_NAME, CREATOR_ID_DB, userId);
            ArrayList<String> arr = new ArrayList<>();
            arr.add(PARENT_SUBTHREAD_ID_DB);
            arr.add(CREATOR_ID_DB);
            arr.add(CONTENT_DB);
            arr.add(PARENT_CONTENT_TYPE_DB);
            arr.add(LIKES_DB);
            arr.add(DISLIKES_DB);
            arr.add(DATE_CREATED_DB);
            response = arango.parseOutput(cursor, COMMENT_ID_DB, arr);

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
        GetMyComments getMyComments = new GetMyComments();
        JSONObject body = new JSONObject();

        JSONObject uriParams = new JSONObject();
        uriParams.put(USER_ID, "Manta");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "GET");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(getMyComments.execute(request));

    }
}
