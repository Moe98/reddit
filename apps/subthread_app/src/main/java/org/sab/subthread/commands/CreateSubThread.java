package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.sab.arango.Arango;
import org.sab.models.NotificationMessages;
import org.sab.models.SubThread;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.notifyApp;
import static org.sab.innerAppComm.Comm.tag;

public class CreateSubThread extends SubThreadCommand {
    final long INITIAL_LIKES = 0;
    final long INITIAL_DISLIKES = 0;

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    protected Schema getSchema() {
        Attribute parentThreadId = new Attribute(PARENT_THREAD_ID, DataType.STRING, true);

        Attribute title = new Attribute(TITLE, DataType.STRING, true);
        Attribute content = new Attribute(CONTENT, DataType.STRING, true);

        Attribute hasImage = new Attribute(HAS_IMAGE, DataType.BOOLEAN, true);

        return new Schema(List.of(parentThreadId, title, content, hasImage));
    }

    @Override
    protected String execute() {

        Arango arango;

        SubThread subThread;

        try {
            String creatorId = authenticationParams.getString(SubThreadCommand.USERNAME);
            String parentThreadId = body.getString(PARENT_THREAD_ID);
            String title = body.getString(TITLE);
            String content = body.getString(CONTENT);
            boolean hasImage = body.getBoolean(HAS_IMAGE);

            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_CREATE_SUBTHREAD_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);
            // TODO check thread exists
            String msg;
            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, parentThreadId)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            BaseDocument myObject = new BaseDocument();

            myObject.addAttribute(PARENT_THREAD_ID_DB, parentThreadId);
            myObject.addAttribute(CREATOR_ID_DB, creatorId);
            myObject.addAttribute(TITLE_DB, title);
            myObject.addAttribute(CONTENT_DB, content);
            myObject.addAttribute(LIKES_DB, INITIAL_LIKES);
            myObject.addAttribute(DISLIKES_DB, INITIAL_DISLIKES);
            myObject.addAttribute(HAS_IMAGE_DB, hasImage);

            if (hasImage) {
                // TODO handle adding the image to the DB

            }

            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            myObject.addAttribute(DATE_CREATED_DB, sqlDate);

            BaseDocument res = arango.createDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, myObject);

            String subThreadId = res.getKey();
            parentThreadId = (String) res.getAttribute(PARENT_THREAD_ID_DB);
            creatorId = (String) res.getAttribute(CREATOR_ID_DB);

            title = (String) res.getAttribute(TITLE_DB);
            content = (String) res.getAttribute(CONTENT_DB);

            String date = (String) res.getAttribute(DATE_CREATED_DB);
            hasImage = (Boolean) res.getAttribute(HAS_IMAGE_DB);

            int likes = Integer.parseInt(String.valueOf(res.getAttribute(LIKES_DB)));
            int dislikes = Integer.parseInt(String.valueOf(res.getAttribute(DISLIKES_DB)));

            // TODO validate correct insertion

            subThread = SubThread.createNewSubThread(parentThreadId, creatorId, title, content, hasImage);
            subThread.setId(subThreadId);
            subThread.setDateCreated(date);
            subThread.setLikes(likes);
            subThread.setDislikes(dislikes);

            // Create an edge between user and subthread.
            final BaseEdgeDocument edgeDocumentFromUserToComment = addEdgeFromUserToSubthread(subThread);

            arango.createEdgeDocument(DB_Name, USER_CREATE_SUBTHREAD_COLLECTION_NAME, edgeDocumentFromUserToComment);

            // tag a person if someone was tagged in the content of the subthread
            tag(Notification_Queue_Name, NotificationMessages.SUBTHREAD_TAG_MSG.getMSG(), subThreadId, content, SEND_NOTIFICATION_FUNCTION_NAME);

            BaseDocument threadDoc = arango.readDocument(DB_Name,  THREAD_COLLECTION_NAME, parentThreadId);
            String threadCreatorId = threadDoc.getAttribute(THREAD_CREATOR_ID_DB).toString();

            // notify the owner of the subthread about the creation
            notifyApp(Notification_Queue_Name, NotificationMessages.SUBTHREAD_CREATE_MSG.getMSG(), subThreadId, threadCreatorId, SEND_NOTIFICATION_FUNCTION_NAME);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }
        return Responder.makeDataResponse(subThread.toJSON());

    }

    private BaseEdgeDocument addEdgeFromUserToSubthread(SubThread subthread) {
        final String creatorId = subthread.getCreatorId();
        final String subthreadId = subthread.getId();
        final String from = USER_COLLECTION_NAME + "/" + creatorId;
        final String to = SUBTHREAD_COLLECTION_NAME + "/" + subthreadId;

        return addEdgeFromTo(from, to);
    }

    private BaseEdgeDocument addEdgeFromTo(String from, String to) {
        BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
        edgeDocument.setFrom(from);
        edgeDocument.setTo(to);

        return edgeDocument;
    }
}
