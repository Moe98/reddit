package org.sab.thread.commands;

import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class BookmarkThread extends ThreadCommand {

    public static void main(String[] args) {
        BookmarkThread bookmarkThread = new BookmarkThread();
        JSONObject body = new JSONObject();
        body.put(THREAD_NAME, "GelatiAzza");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "117690");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(bookmarkThread.execute(request));
    }

    @Override
    protected String execute() {

        Arango arango = null;

        final JSONObject response = new JSONObject();
        String responseMessage = "";

        try {
            final String threadName = body.getString(THREAD_NAME);
            final String userId = uriParams.getString(ACTION_MAKER_ID);
           
            arango = Arango.getInstance();
            arango.connectIfNotConnected();


            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(DB_Name, THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, THREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, true);
            }

            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadName)) {
                responseMessage = "This thread does not exist.";
                return Responder.makeErrorResponse(responseMessage, 400).toString();
            }

            final String bookmarkEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_BOOKMARK_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + userId,
                    THREAD_COLLECTION_NAME + "/" + threadName);

            if (!bookmarkEdgeId.equals("")) {
                responseMessage = "You have removed this Thread from your bookmarks.";
                arango.deleteDocument(DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, bookmarkEdgeId);
            } else {
                responseMessage = "You have added this Thread to your bookmarks!";
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