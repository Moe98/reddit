package org.sab.subthread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.Comment;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class CreateComment extends CommentCommand {
    private Arango arango;
    private ArangoDB arangoDB;

    public static void main(String[] args) {
        CreateComment addComment = new CreateComment();
        JSONObject body = new JSONObject();
        body.put(PARENT_SUBTHREAD_ID, "33029");
        body.put(CREATOR_ID, "67890");
        body.put(CONTENT, "I think their fish is bad!");
        body.put(PARENT_CONTENT_TYPE, "SubThread");

        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(addComment.execute(request));
    }

    @Override
    protected String execute() {
        final int INITIAL_LIKES = 0;
        final int INITIAL_DISLIKES = 0;

        String parentSubThreadId = body.getString(PARENT_SUBTHREAD_ID);
        String creatorId = body.getString(CREATOR_ID);
        String content = body.getString(CONTENT);
        String parentContentType = body.getString(PARENT_CONTENT_TYPE);

        Comment comment;

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, COMMENT_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, false);
            }

            BaseDocument myObject = new BaseDocument();

            myObject.addAttribute("ParentSubThreadId", parentSubThreadId);
            myObject.addAttribute("CreatorId", creatorId);
            myObject.addAttribute("Content", content);
            myObject.addAttribute("ParentContentType", parentContentType);
            myObject.addAttribute("Likes", INITIAL_LIKES);
            myObject.addAttribute("Dislikes", INITIAL_DISLIKES);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            myObject.addAttribute("DateCreated", sqlDate);

            BaseDocument res = arango.createDocument(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, myObject);

            System.out.println(res);
            System.out.println("=========");

            String commentId = res.getKey();
            parentSubThreadId = (String) res.getAttribute("ParentSubThreadId");
            creatorId = (String) res.getAttribute("CreatorId");
            content = (String) res.getAttribute("Content");
            parentContentType = (String) res.getAttribute("ParentContentType");
            final int likes = (int) res.getAttribute("Likes");
            final int dislikes = (int) res.getAttribute("Dislikes");
            String dateCreated = (String) res.getAttribute("DateCreated");

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
            BaseEdgeDocument edgeDocumentFromContentToComment = addEdgeFromContentToComment(comment);

            // Create an edge between user and comment.
            BaseEdgeDocument edgeDocumentFromUserToComment = addEdgeFromUserToComment(comment);

            // Create the edge collections if they do not already exist.
            if (!arango.collectionExists(arangoDB, DB_Name, CONTENT_COMMENT_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, CONTENT_COMMENT_COLLECTION_NAME, true);
            }

            if (!arango.collectionExists(arangoDB, DB_Name, USER_CREATE_COMMENT_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_CREATE_COMMENT_COLLECTION_NAME, true);
            }

            // Add the edge documents.
            arango.createEdgeDocument(arangoDB, DB_Name, CONTENT_COMMENT_COLLECTION_NAME, edgeDocumentFromContentToComment);
            arango.createEdgeDocument(arangoDB, DB_Name, USER_CREATE_COMMENT_COLLECTION_NAME, edgeDocumentFromUserToComment);
        } catch (Exception e) {
            e.printStackTrace();
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
        return Responder.makeDataResponse(comment.toJSON()).toString();
    }

    @Override
    protected Schema getSchema() {
        final Attribute parentSubThreadId = new Attribute(PARENT_SUBTHREAD_ID, DataType.STRING, true);
        final Attribute creatorId = new Attribute(CREATOR_ID, DataType.STRING, true);
        final Attribute content = new Attribute(CONTENT, DataType.STRING, true);
        final Attribute parentContentType = new Attribute(PARENT_CONTENT_TYPE, DataType.STRING, true);

        return new Schema(List.of(parentSubThreadId, creatorId, content, parentContentType));
    }

    private BaseEdgeDocument addEdgeFromContentToComment(Comment comment) {
        final String parentContentType = comment.getParentContentType();
        final String commentId = comment.getId();
        final String parentId = comment.getParentId();

        String from = SubThreadCommand.SUBTHREAD_COLLECTION_NAME + "/" + parentId;
        final String to = COMMENT_COLLECTION_NAME + "/" + commentId;

        // TODO The collection names should come from a config file.
        switch (parentContentType) {
            case "Comment" -> from = COMMENT_COLLECTION_NAME + "/" + parentId;
            case "SubThread" -> from = SubThreadCommand.SUBTHREAD_COLLECTION_NAME + "/" + parentId;
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