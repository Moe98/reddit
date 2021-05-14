package org.sab.thread.commands;

import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class BookmarkThread extends ThreadCommand {
    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {

        Arango arango = null;

        final JSONObject response = new JSONObject();
        String responseMessage = "";

        try {
            final String threadName = body.getString(THREAD_NAME);
            String userId = authenticationParams.getString(ThreadCommand.USERNAME);

            arango = Arango.getInstance();
            arango.connectIfNotConnected();


            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, true);

            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadName)) {
                responseMessage = THREAD_DOES_NOT_EXIST;
                return Responder.makeErrorResponse(responseMessage, 400).toString();
            }

            final String bookmarkEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_BOOKMARK_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + userId,
                    THREAD_COLLECTION_NAME + "/" + threadName);

            if (!bookmarkEdgeId.equals("")) {
                responseMessage = UNBOOKMARKED_THREAD_SUCCESSFULLY;
                arango.deleteDocument(DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, bookmarkEdgeId);
            } else {
                responseMessage = BOOKMARKED_THREAD_SUCCESSFULLY;
                final BaseEdgeDocument userBookmarkThreadEdge = addEdgeFromUserToThread(userId, threadName);
                arango.createEdgeDocument(DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, userBookmarkThreadEdge);
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
            response.put("msg", responseMessage);
        }

        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        final Attribute threadName = new Attribute(THREAD_NAME, DataType.STRING, true);

        return new Schema(List.of(threadName));
    }
}
