package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.Thread;
import org.sab.service.Responder;
import org.sab.validation.Schema;

import java.util.List;

public class GetThread extends ThreadCommand {
    @Override
    protected String execute() {
        final Thread thread;

        try {
            final Arango arango = Arango.getInstance();

            final String threadId = uriParams.getString(THREAD_NAME);

            if (!arango.collectionExists(DB_Name, THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, THREAD_COLLECTION_NAME, false);
            }

            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            final BaseDocument threadDocument = arango.readDocument(DB_Name, THREAD_COLLECTION_NAME, threadId);

            final String description = (String) threadDocument.getAttribute(DESCRIPTION_DB);
            final String creatorId = (String) threadDocument.getAttribute(CREATOR_ID_DB);
            final String dateCreated = (String) threadDocument.getAttribute(DATE_CREATED_DB);
            final int numOfFollowers = Integer.parseInt((String.valueOf(threadDocument.getAttribute(NUM_OF_FOLLOWERS_DB))));

            thread = new Thread();
            thread.setName(threadId);
            thread.setCreatorId(creatorId);
            thread.setDescription(description);
            thread.setDateCreated(dateCreated);
            thread.setNumOfFollowers(numOfFollowers);
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }

        return Responder.makeDataResponse(thread.toJSON()).toString();
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }
}