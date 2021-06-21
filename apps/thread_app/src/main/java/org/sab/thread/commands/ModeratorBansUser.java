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

public class ModeratorBansUser extends ThreadCommand {
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
        String messageResponse = "";

        try {
            final String threadName = body.getString(THREAD_NAME);
            final String bannedUserId = body.getString(BANNED_USER_ID);
            String userId = authenticationParams.getString(ThreadCommand.USERNAME);

            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_MOD_THREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME, true);

            if (!existsInCouchbase(threadName) && !arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadName)) {
                messageResponse = THREAD_DOES_NOT_EXIST;
                return Responder.makeErrorResponse(messageResponse, 400).toString();
            }

            // TODO what if this user is a mod??
            if (!checkUserExists(arango, bannedUserId)) {
                messageResponse = USER_DOES_NOT_EXIST;
                return Responder.makeErrorResponse(messageResponse, 400).toString();
            }

            final String threadModeratorEdgeKey = arango.getSingleEdgeId(DB_Name,
                    USER_MOD_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + userId,
                    THREAD_COLLECTION_NAME + "/" + threadName);
            final String bannedUserEdgeKey = arango.getSingleEdgeId(DB_Name,
                    USER_BANNED_FROM_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + bannedUserId,
                    THREAD_COLLECTION_NAME + "/" + threadName);

            if (threadModeratorEdgeKey.equals("")) {
                // User isn't a moderator of this thread.
                messageResponse = NOT_A_MODERATOR;
                return Responder.makeErrorResponse(messageResponse, 401).toString();
            }
            if (!bannedUserEdgeKey.equals("")) {
                // User is already banned from this thread.
                messageResponse = USER_ALREADY_BANNED;
                return Responder.makeErrorResponse(messageResponse, 400).toString();
            }

            // The request was made from a moderator of this thread, and the
            // user has not been banned yet from the thread.
            final BaseEdgeDocument userBannedFromThreadEdge = addEdgeFromUserToThread(bannedUserId, threadName);
            arango.createEdgeDocument(DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME, userBannedFromThreadEdge);
            messageResponse = USER_BANNED_SUCCESSFULLY;

            // notify the user that he got banned
            notifyApp(Notification_Queue_Name, NotificationMessages.THREAD_USER_BANNED_MSG.getMSG(), threadName, bannedUserId, SEND_NOTIFICATION_FUNCTION_NAME);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            response.put("msg", messageResponse);
        }

        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        final Attribute threadName = new Attribute(THREAD_NAME, DataType.STRING, true);
        final Attribute bannedUserId = new Attribute(BANNED_USER_ID, DataType.STRING, true);

        return new Schema(List.of(threadName, bannedUserId));
    }
}