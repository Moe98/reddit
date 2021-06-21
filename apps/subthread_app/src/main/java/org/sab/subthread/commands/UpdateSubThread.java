package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.CouchbaseBuckets;
import org.sab.models.NotificationMessages;
import org.sab.models.SubThread;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.tag;

public class UpdateSubThread extends SubThreadCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected Schema getSchema() {
        final Attribute content = new Attribute(CONTENT, DataType.STRING, false);
        final Attribute title = new Attribute(TITLE, DataType.STRING, false);

        return new Schema(List.of(content, title));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {
        Arango arango = null;

        final SubThread subthread;

        try {
            arango = Arango.getInstance();

            String content = null, title = null;
            if (body.has(CONTENT))
                content = body.getString(CONTENT);
            if (body.has(TITLE))
                title = body.getString(TITLE);

            String userId = authenticationParams.getString(SubThreadCommand.USERNAME);
            final String subthreadId = uriParams.getString(SUBTHREAD_ID);

            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_COLLECTION_NAME, false);

            boolean subthreadIsCached = false;
            BaseDocument subthreadDocument;

            if(existsInCouchbase(subthreadId)){
                subthreadIsCached = true;
                subthreadDocument = getDocumentFromCouchbase(CouchbaseBuckets.RECOMMENDED_SUB_THREADS.get(), subthreadId);
            }
            else if(existsInArango(SUBTHREAD_COLLECTION_NAME, subthreadId)){
                subthreadDocument = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
            }
            else{
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            final String creatorId = (String) subthreadDocument.getAttribute(CREATOR_ID_DB);

            if (!userId.equals(creatorId)) {
                return Responder.makeErrorResponse(REQUESTER_NOT_AUTHOR, 403);
            }

            if (content != null) {
                subthreadDocument.updateAttribute(CONTENT_DB, content);
            }
            if (title != null) {
                subthreadDocument.updateAttribute(TITLE_DB, title);
            }

            arango.updateDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadDocument, subthreadId);

            final String parentId = (String) subthreadDocument.getAttribute(PARENT_THREAD_ID_DB);
            final String updatedContent = (String) subthreadDocument.getAttribute(CONTENT_DB);
            final String updatedTitle = (String) subthreadDocument.getAttribute(TITLE_DB);
            final int likes = Integer.parseInt(String.valueOf(subthreadDocument.getAttribute(LIKES_DB)));
            final int dislikes = Integer.parseInt(String.valueOf(subthreadDocument.getAttribute(DISLIKES_DB)));
            final String dateCreated = (String) subthreadDocument.getAttribute(DATE_CREATED_DB);
            final boolean hasImage = (Boolean) subthreadDocument.getAttribute(HASIMAGE_DB);

            subthread = new SubThread();
            subthread.setId(subthreadId);
            subthread.setCreatorId(creatorId);
            subthread.setContent(updatedContent);
            subthread.setTitle(updatedTitle);
            subthread.setLikes(likes);
            subthread.setDislikes(dislikes);
            subthread.setDateCreated(dateCreated);
            subthread.setHasImage(hasImage);
            subthread.setParentThreadId(parentId);

            if(subthreadIsCached)
                upsertDocumentInCouchbase(CouchbaseBuckets.RECOMMENDED_SUB_THREADS.get(), subthreadDocument.getKey(), subthreadDocument);

            // tag a person if someone was tagged in the content of the subthread
            tag(Notification_Queue_Name, NotificationMessages.SUBTHREAD_TAG_MSG.getMSG(), subthreadId, content, SEND_NOTIFICATION_FUNCTION_NAME);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeDataResponse(subthread.toJSON());
    }

}
