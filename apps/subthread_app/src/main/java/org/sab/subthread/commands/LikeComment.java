package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.CouchbaseBuckets;
import org.sab.models.NotificationMessages;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.subthread.SubThreadApp;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.notifyApp;


public class LikeComment extends CommentCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected Schema getSchema() {
        Attribute commentId = new Attribute(COMMENT_ID, DataType.STRING, true);
        return new Schema(List.of(commentId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    public String execute() {

        Arango arango;

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            String commentId = body.getString(COMMENT_ID);
            String userId = authenticationParams.getString(CommentCommand.USERNAME);

            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, COMMENT_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, USER_CREATE_COMMENT_COLLECTION_NAME, true);

            String likeEdgeId = arango.getSingleEdgeId(DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, COMMENT_COLLECTION_NAME + "/" + commentId);

            boolean isCommentCached = false;
            BaseDocument originalComment;

            if (commentExistsInCouchbase(commentId)) {
                isCommentCached = true;
                originalComment = getDocumentFromCouchbase(CouchbaseBuckets.COMMENTS.get(), commentId);
            } else if (existsInArango(COMMENT_COLLECTION_NAME, commentId)) {
                originalComment = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);
            } else {
                msg = "Comment does not exist";
                return Responder.makeErrorResponse(msg, 400);
            }

            int newLikes;

            // if user already likes the comment, then remove his like and update like count
            if (!likeEdgeId.equals("")) {
                arango.deleteDocument(DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, likeEdgeId);

                newLikes = Integer.parseInt(String.valueOf(originalComment.getAttribute(LIKES_DB))) - 1;
                originalComment.updateAttribute(LIKES_DB, newLikes);
                // putting the comment with the updated amount of likes
                arango.updateDocument(DB_Name, COMMENT_COLLECTION_NAME, originalComment, commentId);

                msg = "removed your like on the comment";
            } else { // then user wants to like this comment, so we create an edge and update the number of likes
                msg = "added your like on the comment";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(COMMENT_COLLECTION_NAME + "/" + commentId);

                // adding new edgeDocument representing that a user likes a comment
                arango.createEdgeDocument(DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of likes
                newLikes = Integer.parseInt(String.valueOf(originalComment.getAttribute(LIKES_DB))) + 1;
                int newDislikes = Integer.parseInt(String.valueOf(originalComment.getAttribute(DISLIKES_DB)));
                //checking if the user dislikes this content to remove his dislike
                String dislikeEdgeId = arango.getSingleEdgeId(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, COMMENT_COLLECTION_NAME + "/" + commentId);
                if (!dislikeEdgeId.equals("")) {
                    arango.deleteDocument(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, dislikeEdgeId);
                    newDislikes -= 1;
                    msg += " & removed your dislike";
                }
                originalComment.updateAttribute(LIKES_DB, newLikes);
                originalComment.updateAttribute(DISLIKES_DB, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(DB_Name, COMMENT_COLLECTION_NAME, originalComment, commentId);

                BaseDocument commentDoc = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);
                String commentCreatorId = commentDoc.getAttribute(CREATOR_ID_DB).toString();
                notifyApp(Notification_Queue_Name, NotificationMessages.COMMENT_LIKE_MSG.getMSG(), commentId, commentCreatorId, SEND_NOTIFICATION_FUNCTION_NAME);
            }

            if (isCommentCached)
                replaceDocumentInCouchbase(CouchbaseBuckets.COMMENTS.get(), commentId, originalComment);
            else if(newLikes > SubThreadApp.COMMENT_LIKES_CACHING_THRESHOLD){
                upsertDocumentInCouchbase(CouchbaseBuckets.COMMENTS.get(), commentId, originalComment);
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        } finally {
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response);
    }
}
