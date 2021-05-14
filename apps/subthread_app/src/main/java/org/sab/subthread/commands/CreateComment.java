package org.sab.subthread.commands;


import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;

import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.Comment;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;


import java.util.List;

public class CreateComment extends CommentCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    public static JSONObject createCommentReq(String parentId, String content, String parentContentType, String commenterId) {

        JSONObject body = new JSONObject();
        body.put(PARENT_SUBTHREAD_ID, parentId);
        body.put(CONTENT, content);
        body.put(PARENT_CONTENT_TYPE, parentContentType);

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, commenterId);

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);
        return request;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    protected String execute() {

        Arango arango = null;

        final int INITIAL_LIKES = 0;
        final int INITIAL_DISLIKES = 0;

        final Comment comment;

        try {
            String parentSubThreadId = body.getString(PARENT_SUBTHREAD_ID);
            String content = body.getString(CONTENT);
            String parentContentType = body.getString(PARENT_CONTENT_TYPE);
            final String userId = authenticationParams.getString(CommentCommand.USERNAME);

            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            arango.createCollectionIfNotExists(DB_Name, COMMENT_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, CONTENT_COMMENT_COLLECTION_NAME, true);
            arango.createCollectionIfNotExists(DB_Name, USER_CREATE_COMMENT_COLLECTION_NAME, true);

            // TODO check other things exist
            //  If the parent is a subthread check subthread exists
            //  If the parent is a comment check a comment exist

            final BaseDocument myObject = new BaseDocument();

            myObject.addAttribute(PARENT_SUBTHREAD_ID_DB, parentSubThreadId);
            myObject.addAttribute(CREATOR_ID_DB, userId);
            myObject.addAttribute(CONTENT_DB, content);
            myObject.addAttribute(PARENT_CONTENT_TYPE_DB, parentContentType);
            myObject.addAttribute(LIKES_DB, INITIAL_LIKES);
            myObject.addAttribute(DISLIKES_DB, INITIAL_DISLIKES);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            myObject.addAttribute(DATE_CREATED_DB, sqlDate);

            final BaseDocument res = arango.createDocument(DB_Name, COMMENT_COLLECTION_NAME, myObject);

            final String commentId = res.getKey();
            parentSubThreadId = (String) res.getAttribute(PARENT_SUBTHREAD_ID_DB);
            final String creatorId = (String) res.getAttribute(CREATOR_ID_DB);
            content = (String) res.getAttribute(CONTENT_DB);
            parentContentType = (String) res.getAttribute(PARENT_CONTENT_TYPE_DB);
            final int likes = Integer.parseInt(String.valueOf(res.getAttribute(LIKES_DB)));
            final int dislikes = Integer.parseInt(String.valueOf(res.getAttribute(DISLIKES_DB)));
            final String dateCreated = (String) res.getAttribute(DATE_CREATED_DB);

            comment = new Comment();
            comment.setId(commentId);
            comment.setParentId(parentSubThreadId);
            comment.setCreatorId(creatorId);
            comment.setContent(content);
            comment.setParentContentType(parentContentType);
            comment.setLikes(likes);
            comment.setDislikes(dislikes);
            comment.setDateCreated(dateCreated);

            // Create an edge between content and comment.
            final BaseEdgeDocument edgeDocumentFromContentToComment = addEdgeFromContentToComment(comment);

            // Create an edge between user and comment.
            final BaseEdgeDocument edgeDocumentFromUserToComment = addEdgeFromUserToComment(comment);

            // Add the edge documents.
            arango.createEdgeDocument(DB_Name, CONTENT_COMMENT_COLLECTION_NAME, edgeDocumentFromContentToComment);
            arango.createEdgeDocument(DB_Name, USER_CREATE_COMMENT_COLLECTION_NAME, edgeDocumentFromUserToComment);
        } catch (Exception e) {
            e.printStackTrace();
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
        final Attribute parentSubThreadId = new Attribute(PARENT_SUBTHREAD_ID, DataType.STRING, true);
        final Attribute content = new Attribute(CONTENT, DataType.STRING, true);
        final Attribute parentContentType = new Attribute(PARENT_CONTENT_TYPE, DataType.STRING, true);

        return new Schema(List.of(parentSubThreadId, content, parentContentType));
    }

    private BaseEdgeDocument addEdgeFromContentToComment(Comment comment) {
        final String parentContentType = comment.getParentContentType();
        final String commentId = comment.getId();
        final String parentId = comment.getParentId();

        String from = "";
        final String to = COMMENT_COLLECTION_NAME + "/" + commentId;

        // TODO The collection names should come from a config file.
        switch (parentContentType.toLowerCase()) {
            case "comment" -> from = COMMENT_COLLECTION_NAME + "/" + parentId;
            case "subthread" -> from = SubThreadCommand.SUBTHREAD_COLLECTION_NAME + "/" + parentId;
        }

        return addEdgeFromTo(from, to);
    }

    private BaseEdgeDocument addEdgeFromUserToComment(Comment comment) {
        final String creatorId = comment.getCreatorId();
        final String commentId = comment.getId();
        final String from = USER_COLLECTION_NAME + "/" + creatorId;
        final String to = COMMENT_COLLECTION_NAME + "/" + commentId;

        return addEdgeFromTo(from, to);
    }

    private BaseEdgeDocument addEdgeFromTo(String from, String to) {
        BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
        edgeDocument.setFrom(from);
        edgeDocument.setTo(to);

        return edgeDocument;
    }
}