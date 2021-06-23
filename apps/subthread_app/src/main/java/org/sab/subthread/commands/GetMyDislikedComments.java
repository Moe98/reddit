package org.sab.subthread.commands;

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

public class GetMyDislikedComments extends CommentCommand {
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

            String userId = authenticationParams.getString(CommentCommand.USERNAME);

            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, true);

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, userId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }
            ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId);
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
        }
        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }
}
