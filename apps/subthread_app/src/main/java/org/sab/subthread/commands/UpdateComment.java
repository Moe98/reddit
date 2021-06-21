package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.CouchbaseBuckets;
import org.sab.models.NotificationMessages;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.tag;

public class UpdateComment extends CommentCommand {

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

        BaseDocument commentDocument;

        try {
            arango = Arango.getInstance();

            final String content = body.getString(CONTENT);
            String userId = authenticationParams.getString(SubThreadCommand.USERNAME);

            final String commentId = uriParams.getString(COMMENT_ID);

            arango.createCollectionIfNotExists(DB_Name, COMMENT_COLLECTION_NAME, false);

            boolean isCommentCached = false;

            if (commentExistsInCouchbase(commentId)) {
                isCommentCached = true;
                commentDocument = getDocumentFromCouchbase(CouchbaseBuckets.COMMENTS.get(), commentId);
            } else if (existsInArango(COMMENT_COLLECTION_NAME, commentId)) {
                commentDocument = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);
            } else {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404);
            }

            final String creatorId = (String) commentDocument.getAttribute(CREATOR_ID_DB);

            if (!userId.equals(creatorId)) {
                return Responder.makeErrorResponse(REQUESTER_NOT_AUTHOR, 403);
            }

            commentDocument.updateAttribute(CONTENT_DB, content);
            arango.updateDocument(DB_Name, COMMENT_COLLECTION_NAME, commentDocument, commentId);

            // tag a person if someone was tagged in the content of the comment
            tag(Notification_Queue_Name, NotificationMessages.COMMENT_TAG_MSG.getMSG(), commentId, content, SEND_NOTIFICATION_FUNCTION_NAME);

            if (isCommentCached)
                replaceDocumentInCouchbase(CouchbaseBuckets.COMMENTS.get(), commentId, commentDocument);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeDataResponse(baseDocumentToJson(commentDocument));
    }

    @Override
    protected Schema getSchema() {
        final Attribute content = new Attribute(CONTENT, DataType.STRING, true);

        return new Schema(List.of(content));
    }
}