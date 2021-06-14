package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.NotificationMessages;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.notifyApp;
import static org.sab.innerAppComm.Comm.updateRecommendation;

public class FollowThread extends ThreadCommand {
    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
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
            String userId = authenticationParams.getString(ThreadCommand.USERNAME);

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

                // notify the user about the unfollow
                notifyApp(Notification_Queue_Name, NotificationMessages.THREAD_UNFOLLOW_MSG.getMSG(), threadName, userId, SEND_NOTIFICATION_FUNCTION_NAME);

            } else {
                responseMessage = FOLLOWED_THREAD_SUCCESSFULLY;

                final BaseEdgeDocument userFollowsThreadEdge = addEdgeFromUserToThread(userId, threadName);
                arango.createEdgeDocument(DB_Name, USER_FOLLOW_THREAD_COLLECTION_NAME, userFollowsThreadEdge);

                ++followerCount;

                // notify the user about the follow
                notifyApp(Notification_Queue_Name, NotificationMessages.THREAD_FOLLOW_MSG.getMSG(), threadName, userId, SEND_NOTIFICATION_FUNCTION_NAME);
            }

            threadDocument.updateAttribute(NUM_OF_FOLLOWERS_DB, followerCount);

            arango.updateDocument(DB_Name, THREAD_COLLECTION_NAME, threadDocument, threadName);

            // send message to the notification app to update the recommendation list
            updateRecommendation(RECOMENDATION_REQUEST_QUEUE, userId, UPDATE_RECOMMENDED_THREADS_FUNCTION_NAME);
            updateRecommendation(RECOMENDATION_REQUEST_QUEUE, userId, UPDATE_RECOMMENDED_SUBTHREADS_FUNCTION_NAME);


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