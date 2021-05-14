package org.sab.thread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;
import org.sab.service.validation.HTTPMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FollowThread extends ThreadCommand {

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    public static void main(String[] args) {
        FollowThread followThread = new FollowThread();
        JSONObject body = new JSONObject();
        body.put(THREAD_NAME, "GelatiAzza");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "lujine");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

//        System.out.println(followThread.execute(request));

        Arango arango = Arango.getInstance();
        ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollection(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, USER_COLLECTION_NAME + "/lujine");
        ArrayList<String> arr = new ArrayList<>();
        arr.add(NUM_OF_FOLLOWERS_DB);
        arr.add(DESCRIPTION_DB);
        arr.add(CREATOR_ID_DB);
        arr.add(DATE_CREATED_DB);
        JSONArray jsonArr = arango.parseOutput(cursor, THREAD_NAME, arr);
        for (Iterator<Object> it = jsonArr.iterator(); it.hasNext(); ) {
            JSONObject j = (JSONObject) it.next();
            System.out.println(j.toString());
        }
    }

    @Override
    protected String execute() {
        Arango arango = null;
        final JSONObject response = new JSONObject();
        String responseMessage = "";

        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            final String threadName = body.getString(THREAD_NAME);
            final String userId = uriParams.getString(ACTION_MAKER_ID);

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, true);

            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadName)) {
                responseMessage = THREAD_DOES_NOT_EXIST;
                return Responder.makeErrorResponse(responseMessage, 400).toString();
            }

            final String followEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_FOLLOW_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + userId,
                    THREAD_COLLECTION_NAME + "/" + threadName);

            final BaseDocument threadDocument = arango.readDocument(DB_Name, THREAD_COLLECTION_NAME, threadName);
            int followerCount = Integer.parseInt(String.valueOf(threadDocument.getAttribute(ThreadCommand.NUM_OF_FOLLOWERS_DB)));

            if (!followEdgeId.equals("")) {
                responseMessage = UNFOLLOWED_THREAD_SUCCESSFULLY;
                arango.deleteDocument(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, followEdgeId);

                --followerCount;
            } else {
                responseMessage = FOLLOWED_THREAD_SUCCESSFULLY;

                final BaseEdgeDocument userFollowsThreadEdge = addEdgeFromUserToThread(userId, threadName);
                arango.createEdgeDocument(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, userFollowsThreadEdge);

                ++followerCount;
            }

            threadDocument.updateAttribute(NUM_OF_FOLLOWERS_DB, followerCount);

            arango.updateDocument(DB_Name, THREAD_COLLECTION_NAME, threadDocument, threadName);
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