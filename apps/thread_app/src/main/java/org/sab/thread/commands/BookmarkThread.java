package org.sab.thread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class BookmarkThread extends ThreadCommand {
    private Arango arango;
    private ArangoDB arangoDB;

    public static void main(String[] args) {
        BookmarkThread bookmarkThread = new BookmarkThread();
        JSONObject body = new JSONObject();
        body.put(THREAD_NAME, "asmakElRayes7amido");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "67890");

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
        final JSONObject response = new JSONObject();
        String responseMessage = "";

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            final String threadName = body.getString(THREAD_NAME);
            final String userId = uriParams.getString(ACTION_MAKER_ID);

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, THREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, THREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, true);
            }

            final String edgeKey = userId + threadName;

            if (arango.documentExists(arangoDB, DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, edgeKey)) {
                responseMessage = "You have removed this Thread from your bookmarks.";
                arango.deleteDocument(arangoDB, DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, edgeKey);
            } else {
                responseMessage = "You have added this Thread to your bookmarks!";
                final BaseEdgeDocument userBookmarkThreadEdge = addEdgeFromUserToThread(userId, threadName, edgeKey);
                arango.createEdgeDocument(arangoDB, DB_Name, USER_BOOKMARK_THREAD_COLLECTION_NAME, userBookmarkThreadEdge);
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
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