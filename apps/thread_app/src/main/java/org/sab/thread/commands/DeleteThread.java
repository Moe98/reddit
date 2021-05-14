package org.sab.thread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

public class DeleteThread extends ThreadCommand {

    @Override
    protected Schema getSchema() {
        Attribute threadId = new Attribute(THREAD_NAME, DataType.STRING, true);
        return new Schema(List.of(threadId));
    }

    @Override
    protected String execute() {

        Arango arango = null;
        JSONObject response = new JSONObject();
        String msg = "";

        JSONArray subThreadJsonArr;
        JSONArray commentJsonArr;

        // TODO add authentication
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            String threadName = body.getString(THREAD_NAME);
            String userId = uriParams.getString(ACTION_MAKER_ID);

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(DB_Name, THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, THREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, COMMENT_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, COMMENT_COLLECTION_NAME, false);
            }

            // TODO check thread exists
            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadName)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // TODO check person deleting is creator
            BaseDocument threadDoc = arango.readDocument(DB_Name, THREAD_COLLECTION_NAME, threadName);
            String creatorID = (String) threadDoc.getAttribute(CREATOR_ID);
            if (!creatorID.equals(userId)) {
                msg = "You are not authorized to delete this thread!";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // get all children subthreads
            ArangoCursor<BaseDocument> cursor = arango.filterCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, PARENT_THREAD_ID_DB, threadName);

//            ArrayList<String> attribs = new ArrayList(Arrays.asList(SUBTHREAD_TITLE_DB));
            ArrayList<String> attribs = new ArrayList<>();

            subThreadJsonArr = arango.parseOutput(cursor, SUBTHREAD_ID_DB, attribs);

            // get all children comment of subthread
            commentJsonArr = new JSONArray();
            JSONArray commentsToGetChildrenOf = new JSONArray();
            JSONArray currComments;
            for (int i = 0; i < subThreadJsonArr.length(); i++) {
                String subthreadId = subThreadJsonArr.getJSONObject(i).getString(SUBTHREAD_ID_DB);
                cursor = arango.filterCollection(DB_Name, COMMENT_COLLECTION_NAME, PARENT_SUBTHREAD_ID_DB, subthreadId);
                currComments = arango.parseOutput(cursor, COMMENT_ID_DB, attribs);
                commentJsonArr.putAll(currComments);
                commentsToGetChildrenOf.putAll(currComments);
            }

            // get all children comments of comments
            currComments = new JSONArray();
            do {
                for (int i = 0; i < commentsToGetChildrenOf.length(); i++) {
                    JSONObject comment = commentsToGetChildrenOf.getJSONObject(0);
                    String commentId = comment.getString(COMMENT_ID_DB);
                    commentsToGetChildrenOf.remove(0);
                    cursor = arango.filterCollection(DB_Name, COMMENT_COLLECTION_NAME, PARENT_SUBTHREAD_ID_DB, commentId);
                    currComments = arango.parseOutput(cursor, COMMENT_ID_DB, attribs);
                    commentJsonArr.putAll(currComments);
                    commentsToGetChildrenOf.putAll(currComments);
                }
            } while (!currComments.isEmpty());

            int numOfSubThread = subThreadJsonArr.length();
            int numOfComments = commentJsonArr.length();

            // TODO turn into transaction
            // delete thread
            arango.deleteDocument(DB_Name, THREAD_COLLECTION_NAME, threadName);

            // delete thread
            for (int i = 0; i < subThreadJsonArr.length(); i++) {
                String subthreadId = subThreadJsonArr.getJSONObject(i).getString(SUBTHREAD_ID_DB);
                arango.deleteDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
            }

            // delete comments
            for (int i = 0; i < commentJsonArr.length(); i++) {
                String commentId = commentJsonArr.getJSONObject(i).getString(COMMENT_ID_DB);
                arango.deleteDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);
            }

            msg = "Deleted thread: " + threadName + " with it's " + numOfSubThread + " subthreads, and " + numOfComments + " comments.";

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 400).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }

    public static void main(String[] args) {

        DeleteThread tc = new DeleteThread();

        JSONObject body = new JSONObject();
        body.put(THREAD_NAME, "asmakElRayes7amido");

        JSONObject uriParams = new JSONObject();
        uriParams.put("userId", "lujine");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");
        System.out.println(tc.execute(request));

    }
}
