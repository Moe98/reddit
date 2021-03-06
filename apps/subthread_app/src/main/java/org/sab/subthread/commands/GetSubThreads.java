package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

public class GetSubThreads extends SubThreadCommand {

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected String execute() {
        Arango arango;
        JSONArray response;
        try {
            arango = Arango.getInstance();

            // TODO not a uri param
            final String threadId = uriParams.getString(THREAD_ID);

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_COLLECTION_NAME, false);

            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404);
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
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }
        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }
}
