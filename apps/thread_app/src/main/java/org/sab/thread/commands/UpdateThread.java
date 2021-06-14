package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.NotificationMessages;
import org.sab.models.Thread;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.notifyApp;

public class UpdateThread extends ThreadCommand {
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

        final Thread thread;

        try {
            final String description = body.getString(DESCRIPTION);
            final String threadId = uriParams.getString(THREAD_NAME);
            String userId = authenticationParams.getString(ThreadCommand.USERNAME);

            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);

            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            final BaseDocument threadDocument = arango.readDocument(DB_Name, THREAD_COLLECTION_NAME, threadId);

            final String creatorId = (String) threadDocument.getAttribute(CREATOR_ID_DB);

            if (!userId.equals(creatorId)) {
                return Responder.makeErrorResponse(REQUESTER_NOT_AUTHOR, 403).toString();
            }

            threadDocument.updateAttribute(DESCRIPTION_DB, description);
            arango.updateDocument(DB_Name, THREAD_COLLECTION_NAME, threadDocument, threadId);

            final String dateCreated = (String) threadDocument.getAttribute(DATE_CREATED_DB);
            final int numOfFollowers = Integer.parseInt((String.valueOf(threadDocument.getAttribute(NUM_OF_FOLLOWERS_DB))));

            thread = new Thread();
            thread.setName(threadId);
            thread.setCreatorId(creatorId);
            thread.setDescription(description);
            thread.setDateCreated(dateCreated);
            thread.setNumOfFollowers(numOfFollowers);

            // notify the user about the update of the thread
            notifyApp(Notification_Queue_Name, NotificationMessages.THREAD_UPDATE_MSG.getMSG(), threadId, userId, SEND_NOTIFICATION_FUNCTION_NAME);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
        }

        return Responder.makeDataResponse(thread.toJSON()).toString();
    }

    @Override
    protected Schema getSchema() {
        final Attribute description = new Attribute(DESCRIPTION, DataType.STRING, true);

        return new Schema(List.of(description));
    }
}