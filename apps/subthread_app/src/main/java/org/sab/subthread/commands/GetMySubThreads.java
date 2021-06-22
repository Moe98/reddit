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

public class GetMySubThreads extends SubThreadCommand {

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
        Arango arango;
        JSONArray response;
        try {
            arango = Arango.getInstance();

            String userId = authenticationParams.getString(SubThreadCommand.USERNAME);

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_COLLECTION_NAME, false);


            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404);
            }

            ArangoCursor<BaseDocument> cursor = arango.filterCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, CREATOR_ID_DB, userId);
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
