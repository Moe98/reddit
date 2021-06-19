package org.sab.thread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.NotificationMessages;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

import static org.sab.innerAppComm.Comm.notifyApp;

public class DeleteThread extends ThreadCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected Schema getSchema() {
        Attribute threadId = new Attribute(THREAD_NAME, DataType.STRING, true);
        return new Schema(List.of(threadId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.DELETE;
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

            String threadName = body.getString(THREAD_NAME);
            String userId = authenticationParams.getString(ThreadCommand.USERNAME);

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, COMMENT_COLLECTION_NAME, false);

            // TODO check thread exists
            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadName)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // TODO check person deleting is creator
            BaseDocument threadDoc = arango.readDocument(DB_Name, THREAD_COLLECTION_NAME, threadName);
            String creatorID = (String) threadDoc.getAttribute(CREATOR_ID_DB);
            if (!creatorID.equals(userId)) {
                msg = "You are not authorized to delete this thread!";
                return Responder.makeErrorResponse(msg, 401).toString();
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
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }
}

