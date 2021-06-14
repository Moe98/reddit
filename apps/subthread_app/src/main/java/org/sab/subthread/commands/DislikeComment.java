package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
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

public class DislikeComment extends CommentCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected Schema getSchema() {
        Attribute commentId = new Attribute(COMMENT_ID, DataType.STRING, true);
        return new Schema(List.of(commentId));
    }

    @Override
    public String execute() {

        Arango arango = null;

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            String commentId = body.getString(COMMENT_ID);
            String userId = authenticationParams.getString(CommentCommand.USERNAME);
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB

            // TODO if this doesn't exist something is wrong!
            arango.createCollectionIfNotExists(DB_Name, COMMENT_COLLECTION_NAME, false);

            arango.createCollectionIfNotExists(DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, true);

            arango.createCollectionIfNotExists(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, true);

            arango.createCollectionIfNotExists(DB_Name, USER_CREATE_COMMENT_COLLECTION_NAME, true);

            if (!arango.documentExists(DB_Name, COMMENT_COLLECTION_NAME, commentId)) {
                msg = "Comment does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            String dislikeEdgeId = arango.getSingleEdgeId(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, COMMENT_COLLECTION_NAME + "/" + commentId);

            // if user already dislikes the comment, then remove his dislike and update dislike count
            if (!dislikeEdgeId.equals("")) {
                arango.deleteDocument(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, dislikeEdgeId);

                BaseDocument originalComment = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);
                // TODO make this thread safe
                //  I feel like this var is unnecessary to begin with
                int newDisikes = Integer.parseInt(String.valueOf(originalComment.getAttribute(DISLIKES_DB))) - 1;
                originalComment.updateAttribute(DISLIKES_DB, newDisikes);
                // putting the comment with the updated amount of dislikes
                arango.updateDocument(DB_Name, COMMENT_COLLECTION_NAME, originalComment, commentId);

                msg = "removed your dislike on the comment";
            } else { // then user wants to dilike this comment, so we create an edge and update the number of dilikes
                msg = "added your dislike on the comment";

                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(COMMENT_COLLECTION_NAME + "/" + commentId);

                // adding new edgeDocument representing that a user dislikes a comment
                arango.createEdgeDocument(DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of likes
                BaseDocument originalComment = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);
                int newDislikes = Integer.parseInt(String.valueOf(originalComment.getAttribute(DISLIKES_DB))) + 1;
                // TODO why update likes?
                int newLikes = Integer.parseInt(String.valueOf(originalComment.getAttribute(LIKES_DB)));
                //checking if the user likes this content to remove his like
                String likeEdgeId = arango.getSingleEdgeId(DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, COMMENT_COLLECTION_NAME + "/" + commentId);
                if (!likeEdgeId.equals("")) {
                    arango.deleteDocument(DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, likeEdgeId);
                    newLikes -= 1;
                    msg += " & removed your like";
                }
                originalComment.updateAttribute(LIKES_DB, newLikes);
                originalComment.updateAttribute(DISLIKES_DB, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(DB_Name, COMMENT_COLLECTION_NAME, originalComment, commentId);

                ArangoCursor<BaseDocument> cursor = arango.filterEdgeCollectionInbound(DB_Name, USER_CREATE_COMMENT_COLLECTION_NAME, COMMENT_COLLECTION_NAME + "/" + commentId);
                ArrayList<String> arr = new ArrayList<>();
                arr.add(USER_IS_DELETED_DB);
                arr.add(USER_NUM_OF_FOLLOWERS_DB);
                JSONArray commentCreatorArr = arango.parseOutput(cursor, USER_ID_DB, arr);

                if(commentCreatorArr.length()>0){
                    String commentCreator = ((JSONObject)commentCreatorArr.get(0)).getString(USER_ID_DB);
                    // notify the owner of the comment about the dislike
                    notifyApp(Notification_Queue_Name, NotificationMessages.COMMENT_DISLIKE_MSG.getMSG(), commentId, commentCreator, SEND_NOTIFICATION_FUNCTION_NAME);
                }

            }
        } catch (Exception e) {
            // System.out.println(e.getStackTrace());
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }
}
