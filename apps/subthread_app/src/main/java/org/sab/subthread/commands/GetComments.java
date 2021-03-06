package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

public class GetComments extends CommentCommand {
    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }

    @Override
    protected String execute() {
        Arango arango;
        JSONArray response;
        String msg;
        try {
            arango = Arango.getInstance();

            final String parentId = uriParams.getString(PARENT_SUBTHREAD_ID);
            final String parentType = uriParams.getString(PARENT_CONTENT_TYPE);

            final String parentCollection;
            // TODO do better
            if (parentType.equals("Subthread")) {
                parentCollection = SUBTHREAD_COLLECTION_NAME;
            } else {
                parentCollection = COMMENT_COLLECTION_NAME;
            }

            arango.createCollectionIfNotExists(DB_Name, COMMENT_COLLECTION_NAME, false);


            // check if comment exists
            if (!arango.documentExists(DB_Name, parentCollection, parentId)) {
                msg = parentType + " does not exist";
                return Responder.makeErrorResponse(msg, 400);
            }

            // get all children comments at level 1
            ArangoCursor<BaseDocument> cursor = null;

            ArrayList<String> attribs = new ArrayList<>();

            JSONArray commentJsonArr = new JSONArray();
            if (parentType.equals("Subthread")) {
                commentJsonArr = arango.parseOutput(cursor, CommentCommand.COMMENT_ID_DB, attribs);
                JSONArray commentsToGetChildrenOf = new JSONArray();
                commentsToGetChildrenOf.putAll(commentJsonArr);
            } else {
                JSONObject comment = new JSONObject();
                comment.put(COMMENT_ID_DB, parentId);
                commentJsonArr.put(comment);

            }

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

            response = commentJsonArr;

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        }
        return Responder.makeDataResponse(response).toString();
    }

}
