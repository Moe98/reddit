package org.sab.subthread.commands;

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

public class DeleteSubThread extends SubThreadCommand {

    @Override
    protected Schema getSchema() {
        Attribute threadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        return new Schema(List.of(threadId));
    }

    @Override
    protected String execute() {

        Arango arango = null;
        JSONObject response = new JSONObject();
        String msg = "";

        JSONArray commentJsonArr;

        // TODO add authentication
        try {
            String subthreadId = body.getString(SUBTHREAD_ID);
            String userId = uriParams.getString(ACTION_MAKER_ID);

            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(DB_Name, SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, CommentCommand.COMMENT_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, false);
            }

            // check if subthread exists
            if (!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // check person deleting is creator
            BaseDocument subthreadDoc = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
            String creatorID = (String) subthreadDoc.getAttribute(CREATOR_ID);
            if (!creatorID.equals(userId)) {
                msg = "You are not authorized to delete this thread!";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // get all children comments at level 1
            ArangoCursor<BaseDocument> cursor = arango.filterCollection(DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, CommentCommand.PARENT_SUBTHREAD_ID_DB, subthreadId);

//            ArrayList<String> attribs = new ArrayList(Arrays.asList(SUBTHREAD_TITLE_DB));
            ArrayList<String> attribs = new ArrayList<>();

            commentJsonArr = arango.parseOutput(cursor, CommentCommand.COMMENT_ID_DB, attribs);
            JSONArray commentsToGetChildrenOf = new JSONArray();
            commentsToGetChildrenOf.putAll(commentJsonArr);

            // get all children comments of comments
            JSONArray currComments = new JSONArray();
            do {
                for (int i = 0; i < commentsToGetChildrenOf.length(); i++) {
                    JSONObject comment = commentsToGetChildrenOf.getJSONObject(0);
                    String commentId = comment.getString(CommentCommand.COMMENT_ID_DB);
                    commentsToGetChildrenOf.remove(0);
                    cursor = arango.filterCollection(DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, CommentCommand.PARENT_SUBTHREAD_ID_DB, commentId);
                    currComments = arango.parseOutput(cursor, CommentCommand.COMMENT_ID_DB, attribs);
                    commentJsonArr.putAll(currComments);
                    commentsToGetChildrenOf.putAll(currComments);
                }
            } while (!currComments.isEmpty());

            int numOfComments = commentJsonArr.length();

            // TODO turn into transaction
            // delete subthread
            arango.deleteDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);

            // delete comments
            for (int i = 0; i < commentJsonArr.length(); i++) {
                String commentId = commentJsonArr.getJSONObject(i).getString(CommentCommand.COMMENT_ID_DB);
                arango.deleteDocument(DB_Name, CommentCommand.COMMENT_COLLECTION_NAME, commentId);
            }

            msg = "Deleted subthread: " + subthreadId + " with it's " + numOfComments + " comments.";

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

}