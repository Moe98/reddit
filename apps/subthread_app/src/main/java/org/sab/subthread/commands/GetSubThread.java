package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.SubThread;
import org.sab.service.Responder;
import org.sab.validation.Schema;
import org.sab.service.validation.HTTPMethod;

import java.util.List;

public class GetSubThread extends SubThreadCommand {

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected String execute() {
        Arango arango = null;
        final SubThread subThread;

        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            final String subThreadId = uriParams.getString(SUBTHREAD_ID);

            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_COLLECTION_NAME, false);

            if (!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subThreadId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            final BaseDocument subThreadDocument = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subThreadId);

            final String parentThreadId = (String) subThreadDocument.getAttribute(PARENT_THREAD_ID_DB);
            final String creatorId = (String) subThreadDocument.getAttribute(CREATOR_ID_DB);
            final String title = (String) subThreadDocument.getAttribute(TITLE_DB);
            final String content = (String) subThreadDocument.getAttribute(CONTENT_DB);
            final String date = (String) subThreadDocument.getAttribute(DATE_CREATED_DB);
            final boolean hasImage = (Boolean) subThreadDocument.getAttribute(HASIMAGE_DB);
            final int likes = Integer.parseInt(String.valueOf(subThreadDocument.getAttribute(LIKES_DB)));
            final int dislikes = Integer.parseInt(String.valueOf(subThreadDocument.getAttribute(DISLIKES_DB)));

            subThread = SubThread.createNewSubThread(parentThreadId, creatorId, title, content, hasImage);
            subThread.setId(subThreadId);
            subThread.setDateCreated(date);
            subThread.setLikes(likes);
            subThread.setDislikes(dislikes);
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
        }

        return Responder.makeDataResponse(subThread.toJSON()).toString();
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }
}