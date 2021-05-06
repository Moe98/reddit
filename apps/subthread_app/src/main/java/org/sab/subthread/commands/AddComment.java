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

public class AddComment extends CommentCommand {
    private Arango arango;
    private ArangoDB arangoDB;

    public static void main(String[] args) {
        AddComment addComment = new AddComment();
        JSONObject body = new JSONObject();
        body.put(PARENT_SUBTHREAD_ID, "12345");
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
        final String collectionName = "Comment";
        final String userCollectionName = "User";
        final String DBName = "ARANGO_DB";

        String parentSubThreadId = body.getString(PARENT_SUBTHREAD_ID);
        String creatorId = body.getString(CREATOR_ID);
        String content = body.getString(CONTENT);
        String parentContentType = body.getString(PARENT_CONTENT_TYPE);

        Comment comment;

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, collectionName)) {
                arango.createCollection(arangoDB, DBName, collectionName, false);
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

            BaseDocument res = arango.createDocument(arangoDB, DBName, collectionName, myObject);

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
            comment.setParentId(parentSubThreadId);
            comment.setCreatorId(creatorId);
            comment.setContent(content);
            comment.setParentContentType(parentContentType);
            comment.setLikes(likes);
            comment.setDislikes(dislikes);
            comment.setDateCreated(dateCreated);

            // Create an edge between content and comment.
            BaseEdgeDocument edgeDocumentFromContentToComment = addEdgeFromContentToComment(comment, commentId);

            // Create an edge between user and comment.
            BaseEdgeDocument edgeDocumentFromUserToComment = addEdgeFromUserToComment(comment, commentId);

            final String contentCollectionName = getCollectionName(parentContentType);

            // Create the edge collections if they do not already exist.
            if (!arango.collectionExists(arangoDB, DBName, contentCollectionName)) {
                arango.createCollection(arangoDB, DBName, contentCollectionName, true);
            }

            if (!arango.collectionExists(arangoDB, DBName, userCollectionName)) {
                arango.createCollection(arangoDB, DBName, userCollectionName, true);
            }

            // Add the edge documents.
            arango.createEdgeDocument(arangoDB, DBName, contentCollectionName, edgeDocumentFromContentToComment);
            arango.createEdgeDocument(arangoDB, DBName, userCollectionName, edgeDocumentFromUserToComment);
        } catch (Exception e) {
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

    private BaseEdgeDocument addEdgeFromContentToComment(Comment comment, String commentId) {
        final String parentContentType = comment.getParentContentType();
        final String parentId = comment.getParentId();
        final String edgeKey = createEdgeKey(parentId, commentId);

        String from = "SubThread/" + parentId;
        final String to = "Comment/" + commentId;

        // TODO The collection names should come from a config file.
        switch (parentContentType) {
            case "Comment" -> from = "Comment/" + parentId;
            case "SubThread" -> from = "SubThread/" + parentId;
        }

        return addEdgeFromToWithKey(from, to, edgeKey);
    }

    private BaseEdgeDocument addEdgeFromUserToComment(Comment comment, String commentId) {
        final String creatorId = comment.getCreatorId();
        final String from = "User/" + creatorId;
        final String to = "Comment/" + commentId;
        final String edgeKey = createEdgeKey(creatorId, commentId);

        return addEdgeFromToWithKey(from, to, edgeKey);
    }

    private BaseEdgeDocument addEdgeFromToWithKey(String from, String to, String key) {
        BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
        edgeDocument.setFrom(from);
        edgeDocument.setTo(to);
        edgeDocument.setTo(key);

        return edgeDocument;
    }

    private String createEdgeKey(String creatorId, String commentId) {
        return creatorId + commentId;
    }

    private String getCollectionName(String collection) {
        return switch (collection) {
            case "SubThread" -> "SubThread";
            case "Comment" -> "Comment";
            default -> "";
        };
    }
}