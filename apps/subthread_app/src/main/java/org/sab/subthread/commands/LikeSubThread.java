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

public class LikeSubThread extends SubThreadCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected Schema getSchema() {
        Attribute subthreadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        return new Schema(List.of(subthreadId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    public String execute() {

        Arango arango = null;

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            String subthreadId = body.getString(SUBTHREAD_ID);
            String userId = authenticationParams.getString(SubThreadCommand.USERNAME);
            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, USER_CREATE_SUBTHREAD_COLLECTION_NAME, true);

            if (!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            String likeEdgeId = arango.getSingleEdgeId(DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

            // if user already likes the subthread, then remove his like and update like count
            if (!likeEdgeId.equals("")) {
                arango.deleteDocument(DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, likeEdgeId);

                BaseDocument originalSubthread = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newLikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(LIKES_DB))) - 1;
                originalSubthread.updateAttribute(LIKES_DB, newLikes);
                // putting the comment with the updated amount of likes
                arango.updateDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, originalSubthread, subthreadId);

                msg = "removed your like on the subthread";
            } else { // then user wants to like this subthread, so we create an edge and update the number of likes
                msg = "added your like on the subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

                // adding new edgeDocument representing that a user likes a comment
                arango.createEdgeDocument(DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of likes and dislikes
                BaseDocument originalSubthread = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newLikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(LIKES_DB))) + 1;
                int newDislikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(DISLIKES_DB)));

                String dislikeEdgeId = arango.getSingleEdgeId(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);
                //checking if the user dislikes this subthread to remove his dislike
                if (!dislikeEdgeId.equals("")) {
                    arango.deleteDocument(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, dislikeEdgeId);
                    newDislikes -= 1;
                    msg += " & removed your dislike";
                }
                originalSubthread.updateAttribute(LIKES_DB, newLikes);
                originalSubthread.updateAttribute(DISLIKES_DB, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, originalSubthread, subthreadId);

                BaseDocument subthreadDoc = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                String subthreadCreatorId = subthreadDoc.getAttribute(CREATOR_ID_DB).toString();
                notifyApp(Notification_Queue_Name, NotificationMessages.SUBTHREAD_LIKE_MSG.getMSG(), subthreadId, subthreadCreatorId, SEND_NOTIFICATION_FUNCTION_NAME);


            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }
}
