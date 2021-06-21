package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.CouchbaseBuckets;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.List;

public class GetComment extends CommentCommand {
    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected String execute() {

        Arango arango;

        BaseDocument commentDocument;

        try {
            final String commentId = uriParams.getString(COMMENT_ID);

            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, COMMENT_COLLECTION_NAME, false);


            if (commentExistsInCouchbase(commentId)) {
                commentDocument = getDocumentFromCouchbase(CouchbaseBuckets.COMMENTS.get(), commentId);
            } else if (existsInArango(COMMENT_COLLECTION_NAME, commentId)) {
                commentDocument = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);
            } else {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404);
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeDataResponse(baseDocumentToJson(commentDocument));
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }
}