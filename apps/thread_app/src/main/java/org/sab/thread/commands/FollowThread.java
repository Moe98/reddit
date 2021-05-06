package org.sab.thread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class FollowThread extends ThreadCommand {
    private Arango arango;
    private ArangoDB arangoDB;

    public static void main(String[] args) {
        FollowThread addComment = new FollowThread();
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

        System.out.println(addComment.execute(request));
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
            if (!arango.collectionExists(arangoDB, DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, true);
            }

            final String edgeKey = userId + threadName;

            final BaseDocument threadDocument = arango.readDocument(arangoDB, DB_Name, THREAD_COLLECTION_NAME, threadName);
            int followerCount = (int) threadDocument.getAttribute(NUM_OF_FOLLOWERS_DB);

            if (arango.documentExists(arangoDB, DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, edgeKey)) {
                responseMessage = "You have unfollowed this Thread.";
                arango.deleteDocument(arangoDB, DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, edgeKey);

                --followerCount;
            } else {
                responseMessage = "You are now following this Thread!";

                final BaseEdgeDocument userFollowsThreadEdge = addEdgeFromUserToThread(userId, threadName, edgeKey);
                arango.createEdgeDocument(arangoDB, DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, userFollowsThreadEdge);

                ++followerCount;
            }

            threadDocument.updateAttribute(NUM_OF_FOLLOWERS_DB, ++followerCount);

            arango.updateDocument(arangoDB, DB_Name, THREAD_COLLECTION_NAME, threadDocument, threadName);
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