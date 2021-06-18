package org.sab.subthread.commands;

import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class BookmarkSubThread extends SubThreadCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected Schema getSchema() {
        Attribute subthreadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        return new Schema(List.of(subthreadId));
    }

    @Override
    protected String execute() {

        Arango arango = null;

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            String subthreadId = body.getString(SUBTHREAD_ID);
            String userId = authenticationParams.getString(USERNAME);

            arango = Arango.getInstance();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(DB_Name, SUBTHREAD_COLLECTION_NAME)) {
                // TODO if this doesn't exist something is wrong!
                arango.createCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, true);
            }

            // check subthread exist
            if (!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            String userBookmarkEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + userId,
                    SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

            if (!userBookmarkEdgeId.equals("")) {
                msg = "Removed Subthread from Bookmarks";
                // unbookmark
                arango.deleteDocument(DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, userBookmarkEdgeId);

            } else {
                // bookmark
                msg = "Bookmarked Subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);
                arango.createEdgeDocument(DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, edgeDocument);

            }


        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();

        }

        return Responder.makeDataResponse(response).toString();

    }

}
