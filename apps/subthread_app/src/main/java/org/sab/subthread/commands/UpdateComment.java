package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.Comment;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class UpdateComment extends CommentCommand {

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {

        Arango arango = null;

        final Comment comment;

        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            final String content = body.getString(CONTENT);
            final String userId = uriParams.getString(ACTION_MAKER_ID);
            final String commentId = uriParams.getString(COMMENT_ID);

            if (!arango.collectionExists(DB_Name, COMMENT_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, COMMENT_COLLECTION_NAME, false);
            }

            if (!arango.documentExists(DB_Name, COMMENT_COLLECTION_NAME, commentId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            final BaseDocument commentDocument = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);

            final String creatorId = (String) commentDocument.getAttribute(CREATOR_ID_DB);

            if (!userId.equals(creatorId)) {
                return Responder.makeErrorResponse(REQUESTER_NOT_AUTHOR, 403).toString();
            }

            commentDocument.updateAttribute(CONTENT_DB, content);
            arango.updateDocument(DB_Name, COMMENT_COLLECTION_NAME, commentDocument, commentId);

            final String parentId = (String) commentDocument.getAttribute(PARENT_SUBTHREAD_ID_DB);
            final String updatedContent = (String) commentDocument.getAttribute(CONTENT_DB);
            final String parentContentType = (String) commentDocument.getAttribute(PARENT_CONTENT_TYPE_DB);
            final int likes = Integer.parseInt(String.valueOf(commentDocument.getAttribute(LIKES_DB)));
            final int dislikes = Integer.parseInt(String.valueOf(commentDocument.getAttribute(DISLIKES_DB)));
            final String dateCreated = (String) commentDocument.getAttribute(DATE_CREATED_DB);

            comment = new Comment();
            comment.setId(commentId);
            comment.setParentId(parentId);
            comment.setCreatorId(creatorId);
            comment.setContent(updatedContent);
            comment.setParentContentType(parentContentType);
            comment.setLikes(likes);
            comment.setDislikes(dislikes);
            comment.setDateCreated(dateCreated);
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
        }

        return Responder.makeDataResponse(comment.toJSON()).toString();
    }

    @Override
    protected Schema getSchema() {
        final Attribute content = new Attribute(CONTENT, DataType.STRING, true);

        return new Schema(List.of(content));
    }
}