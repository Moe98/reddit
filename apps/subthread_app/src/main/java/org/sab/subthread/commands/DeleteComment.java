package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.CouchbaseBuckets;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

public class DeleteComment extends CommentCommand {
    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.DELETE;
    }

    @Override
    protected Schema getSchema() {
        Attribute commentId = new Attribute(COMMENT_ID, DataType.STRING, true);
        return new Schema(List.of(commentId));
    }

    @Override
    protected String execute() {

        Arango arango;
        JSONObject response = new JSONObject();
        String msg = "";

        JSONArray commentJsonArr;

        try {
            String commentId = body.getString(COMMENT_ID);
            String userId = authenticationParams.getString(CommentCommand.USERNAME);
            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);

            arango.createCollectionIfNotExists(DB_Name, COMMENT_COLLECTION_NAME, false);

            // check if comment exists
            if(!commentExistsInCouchbase(commentId) && !existsInArango(COMMENT_COLLECTION_NAME, commentId)){
                msg = "Comment does not exist";
                return Responder.makeErrorResponse(msg, 400);
            }

            // check person deleting is creator
            BaseDocument subthreadDoc = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);
            String creatorID = (String) subthreadDoc.getAttribute(CREATOR_ID_DB);
            if (!creatorID.equals(userId)) {
                msg = "You are not authorized to delete this comment!";
                return Responder.makeErrorResponse(msg, 401);
            }

            // get all children comments at level 1
            ArangoCursor<BaseDocument> cursor;

            ArrayList<String> attribs = new ArrayList<>();

            commentJsonArr = new JSONArray();
            JSONObject comment = new JSONObject();
            comment.put(COMMENT_ID_DB, commentId);
            commentJsonArr.put(comment);

            JSONArray commentsToGetChildrenOf = new JSONArray();
            commentsToGetChildrenOf.putAll(commentJsonArr);

            // get all children comments of comments
            JSONArray currComments = new JSONArray();
            do {
                for (int i = 0; i < commentsToGetChildrenOf.length(); i++) {
                    JSONObject currComment = commentsToGetChildrenOf.getJSONObject(0);
                    String currCommentId = currComment.getString(COMMENT_ID_DB);
                    commentsToGetChildrenOf.remove(0);
                    cursor = arango.filterCollection(DB_Name, COMMENT_COLLECTION_NAME, PARENT_SUBTHREAD_ID_DB, currCommentId);
                    currComments = arango.parseOutput(cursor, COMMENT_ID_DB, attribs);
                    commentJsonArr.putAll(currComments);
                    commentsToGetChildrenOf.putAll(currComments);
                }
            } while (!currComments.isEmpty());

            int numOfComments = commentJsonArr.length() - 1;

            // TODO turn into transaction

            // delete comments
            for (int i = 0; i < commentJsonArr.length(); i++) {
                String currCommentId = commentJsonArr.getJSONObject(i).getString(COMMENT_ID_DB);
                arango.deleteDocument(DB_Name, COMMENT_COLLECTION_NAME, currCommentId);
                deleteDocumentFromCouchbase(CouchbaseBuckets.COMMENTS.get(), currCommentId);
            }

            msg = "Deleted comment: with it's " + numOfComments + " nested comments.";

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        } finally {
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response);
    }
}
