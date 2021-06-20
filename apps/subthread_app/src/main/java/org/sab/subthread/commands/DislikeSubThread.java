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

public class DislikeSubThread extends SubThreadCommand {
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
        Attribute subthreadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        return new Schema(List.of(subthreadId));
    }

    @Override
    public String execute() {

        Arango arango = null;

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            String subthreadId = body.getString(SUBTHREAD_ID);
            String userId = authenticationParams.getString(CommentCommand.USERNAME);
            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, USER_CREATE_SUBTHREAD_COLLECTION_NAME, true);

            // TODO check if subthread exists
            if (!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            String dislikeEdgeId = arango.getSingleEdgeId(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);
            // if user already dislikes the subthread, then remove his dislike and update dislike count
            if (!dislikeEdgeId.equals("")) {
                arango.deleteDocument(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, dislikeEdgeId);

                BaseDocument originalSubthread = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newDisikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(DISLIKES_DB))) - 1;
                originalSubthread.updateAttribute(DISLIKES_DB, newDisikes);
                // putting the comment with the updated amount of dislikes
                arango.updateDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, originalSubthread, subthreadId);

                msg = "removed your dislike on the subthread";
            } else { // then user wants to dislike this subthread, so we create an edge and update the number of dislikes
                msg = "added your dislike on the subthread";

                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

                // adding new edgeDocument representing that a user dislikes a subthread
                arango.createEdgeDocument(DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of dislikes and likes
                BaseDocument originalSubthread = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newDislikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(DISLIKES_DB))) + 1;
                int newLikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(LIKES_DB)));
                String likeEdgeId = arango.getSingleEdgeId(DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, USER_COLLECTION_NAME + "/" + userId, SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);
                //checking if the user likes this subthread to remove his like
                if (!likeEdgeId.equals("")) {
                    arango.deleteDocument(DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, likeEdgeId);
                    newLikes -= 1;
                    msg += " & removed your like";
                }
                originalSubthread.updateAttribute(LIKES_DB, newLikes);
                originalSubthread.updateAttribute(DISLIKES_DB, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, originalSubthread, subthreadId);

                BaseDocument subthreadDoc = arango.readDocument(DB_Name,  SUBTHREAD_COLLECTION_NAME, subthreadId);
                String subthreadCreatorId = subthreadDoc.getAttribute(CREATOR_ID_DB).toString();
                notifyApp(Notification_Queue_Name, NotificationMessages.SUBTHREAD_DISLIKE_MSG.getMSG(), subthreadId, subthreadCreatorId, SEND_NOTIFICATION_FUNCTION_NAME);


            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }
}
