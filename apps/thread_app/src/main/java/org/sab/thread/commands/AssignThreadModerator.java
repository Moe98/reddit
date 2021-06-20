package org.sab.thread.commands;

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

public class AssignThreadModerator extends ThreadCommand {
    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected Schema getSchema() {
        Attribute parentSubthreadId = new Attribute(THREAD_NAME, DataType.STRING, true);
        Attribute newModeratorId = new Attribute(MODERATOR_ID, DataType.STRING, true);

        return new Schema(List.of(parentSubthreadId, newModeratorId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {

        Arango arango = null;
        JSONObject response = new JSONObject();
        String msg = "";

        try {

            String threadId = body.getString(THREAD_NAME);
            String modId = body.getString(MODERATOR_ID);

            String assignerId = authenticationParams.getString(ThreadCommand.USERNAME);

            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_MOD_THREAD_COLLECTION_NAME, true);

            // check if thread exists
            if (!existsInCouchbase(threadId) && !arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadId)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // check if assigner is a moderator on this thread
            String assignerModEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_MOD_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + assignerId,
                    THREAD_COLLECTION_NAME + "/" + threadId);
            if (assignerModEdgeId.equals("")) {
                // assigner is not a mod
                msg = "You don't have permission to assign a moderator for this thread";
                return Responder.makeErrorResponse(msg, 401).toString();
            }

            String moderatorModEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_MOD_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + modId,
                    THREAD_COLLECTION_NAME + "/" + threadId);

            if (!moderatorModEdgeId.equals("")) {
                msg = "User already moderates this thread";
                return Responder.makeErrorResponse(msg, 400).toString();

            } else {
                // bookmark
                msg = "Assigned Moderator";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + modId);
                edgeDocument.setTo(THREAD_COLLECTION_NAME + "/" + threadId);
                arango.createEdgeDocument(DB_Name, USER_MOD_THREAD_COLLECTION_NAME, edgeDocument);

                // notify the user that he is now a moderator
                notifyApp(Notification_Queue_Name, NotificationMessages.THREAD_USER_IS_MOD_MSG.getMSG(), threadId, modId, SEND_NOTIFICATION_FUNCTION_NAME);

                // notify the "mod" that he is assigned a moderator
                notifyApp(Notification_Queue_Name, NotificationMessages.THREAD_MOD_ASSIGNED_MOD_MSG.getMSG(), threadId, assignerId, SEND_NOTIFICATION_FUNCTION_NAME);

            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();

        } finally {
            if (arango != null) {

            }

            response.put("msg", msg);
        }

        return Responder.makeDataResponse(response).toString();

    }

}
