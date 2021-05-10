package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.Comment;
import org.sab.service.Responder;
import org.sab.validation.Schema;

import java.util.List;

public class GetComment extends CommentCommand {
    @Override
    protected String execute() {
        final Comment comment;

        try {
            final Arango arango = Arango.getInstance();

            final String commentId = uriParams.getString(COMMENT_ID);

            if (!arango.collectionExists(DB_Name, COMMENT_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, COMMENT_COLLECTION_NAME, false);
            }

            if (!arango.documentExists(DB_Name, COMMENT_COLLECTION_NAME, commentId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404).toString();
            }

            final BaseDocument commentDocument = arango.readDocument(DB_Name, COMMENT_COLLECTION_NAME, commentId);

            final String parentId = (String) commentDocument.getAttribute(PARENT_SUBTHREAD_ID_DB);
            final String userId = (String) commentDocument.getAttribute(ACTION_MAKER_ID);
            final String content = (String) commentDocument.getAttribute(CONTENT_DB);
            final String parentContentType = (String) commentDocument.getAttribute(PARENT_CONTENT_TYPE_DB);
            final int likes = Integer.parseInt(String.valueOf(commentDocument.getAttribute(LIKES_DB)));
            final int dislikes = Integer.parseInt(String.valueOf(commentDocument.getAttribute(DISLIKES_DB)));
            final String dateCreated = (String) commentDocument.getAttribute(DATE_CREATED_DB);

            comment = new Comment();
            comment.setId(commentId);
            comment.setParentId(parentId);
            comment.setCreatorId(userId);
            comment.setContent(content);
            comment.setParentContentType(parentContentType);
            comment.setLikes(likes);
            comment.setDislikes(dislikes);
            comment.setDateCreated(dateCreated);
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }

        return Responder.makeDataResponse(comment.toJSON()).toString();
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }
}