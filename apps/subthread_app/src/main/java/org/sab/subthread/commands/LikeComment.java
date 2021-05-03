package org.sab.subthread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class LikeComment extends CommentCommand{
    @Override
    protected Schema getSchema() {
        Attribute parentSubthreadId = new Attribute(COMMENT_ID, DataType.STRING, true);
        return new Schema(List.of(parentSubthreadId));
    }

    private Arango arango;
    private ArangoDB arangoDB;
    private String CommentCollectionName;
    private String CommentEdgeCollectionName;
    private String DBName;

    @Override
    public String execute() {
        String commentId = body.getString(COMMENT_ID);
        String userId = uriParams.getString(USER_ID);

        CommentCollectionName = "Comment";
        CommentEdgeCollectionName = "UserLikeComment";
        DBName = "ARANGO_DB";

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, CommentCollectionName)) {
                arango.createCollection(arangoDB, DBName, CommentCollectionName, false);
            }
            if (!arango.collectionExists(arangoDB, DBName, CommentEdgeCollectionName)) {
                arango.createCollection(arangoDB, DBName, CommentEdgeCollectionName, true);
            }

            BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
            edgeDocument.setFrom("Users" + userId);
            edgeDocument.setTo("Threads" + commentId);

            // adding new edgeDocument representing that a user liked a comment
            arango.createEdgeDocument(arangoDB, DBName, CommentEdgeCollectionName, edgeDocument);

            // retrieving the original comment with the old amount of likes
            BaseDocument originalComment = arango.readDocument(arangoDB, DBName, CommentCollectionName, commentId);
            int newLikes =  (int)originalComment.getAttribute(LIKES)+1;
            originalComment.updateAttribute(LIKES,newLikes);
            // putting the comment with the updated amount of likes
            arango.updateDocument(arangoDB,DBName,CommentCollectionName,originalComment,commentId);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
        return Responder.makeDataResponse(new JSONObject()).toString();
    }




    public static void main(String[] args) {
        LikeComment lc = new LikeComment();
        JSONObject request = new JSONObject("{\"body\":{\"commentId\":\"1998-2-9\"},\"uriParams\":{\"userId\":\"asdasda\"},\"methodType\":\"PUT\"}");
        System.out.println(lc.execute(request));
    }
}
