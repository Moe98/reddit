package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.CouchbaseBuckets;
import org.sab.models.Thread;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

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
        Arango arango;

        final Thread thread;

        try {
            final String description = body.getString(DESCRIPTION);
            final String threadId = uriParams.getString(THREAD_NAME);
            String userId = authenticationParams.getString(ThreadCommand.USERNAME);

            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);

            boolean threadIsCached = false;
            BaseDocument threadDocument;

            if (existsInCouchbase(threadId)) {
                threadIsCached = true;
                threadDocument = getDocumentFromCouchbase(CouchbaseBuckets.RECOMMENDED_THREADS.get(), threadId);
            } else if (existsInArango(THREAD_COLLECTION_NAME, threadId)) {
                threadDocument = arango.readDocument(DB_Name, THREAD_COLLECTION_NAME, threadId);
            } else {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404);
            }

            final String creatorId = (String) threadDocument.getAttribute(CREATOR_ID_DB);

            if (!userId.equals(creatorId)) {
                return Responder.makeErrorResponse(REQUESTER_NOT_AUTHOR, 403);
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

            if(threadIsCached)
                replaceDocumentFromCouchbase(CouchbaseBuckets.RECOMMENDED_THREADS.get(), threadId, threadDocument);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeDataResponse(thread.toJSON());
    }

    @Override
    protected Schema getSchema() {
        final Attribute description = new Attribute(DESCRIPTION, DataType.STRING, true);

        return new Schema(List.of(description));
    }
}