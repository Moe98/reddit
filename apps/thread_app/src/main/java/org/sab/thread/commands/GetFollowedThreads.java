package org.sab.thread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

public class GetFollowedThreads extends ThreadCommand {
    @Override
    protected boolean isAuthNeeded() {
        return true;
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

            String userId = authenticationParams.getString(ThreadCommand.USERNAME);

            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, true);
            }

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }
            ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId);
            ArrayList<String> arr = new ArrayList<>();
            arr.add(NUM_OF_FOLLOWERS_DB);
            arr.add(DESCRIPTION_DB);
            arr.add(CREATOR_ID_DB);
            arr.add(DATE_CREATED_DB);
            response = arango.parseOutput(cursor, THREAD_NAME, arr);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
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
}
