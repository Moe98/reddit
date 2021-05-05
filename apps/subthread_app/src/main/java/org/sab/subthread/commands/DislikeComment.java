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

public class DislikeComment extends CommentCommand{
    @Override
    protected Schema getSchema() {
        Attribute commentId = new Attribute(COMMENT_ID, DataType.STRING, true);
        return new Schema(List.of(commentId));
    }

    private Arango arango;
    private ArangoDB arangoDB;
    private String CommentCollectionName;
    private String UserLikeCommentCollection;
    private String UserDislikeCommentCollection;
    private String DBName;

    @Override
    public String execute() {
        String commentId = body.getString(COMMENT_ID);
        String userId = uriParams.getString(USER_ID);

        CommentCollectionName = "Comment";
        UserLikeCommentCollection = "UserLikeComment";
        UserDislikeCommentCollection = "UserDislikeComment";
        DBName = "ARANGO_DB";

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, CommentCollectionName)) {
                arango.createCollection(arangoDB, DBName, CommentCollectionName, false);
            }
            if (!arango.collectionExists(arangoDB, DBName, UserLikeCommentCollection)) {
                arango.createCollection(arangoDB, DBName, UserLikeCommentCollection, true);
            }
            if (!arango.collectionExists(arangoDB, DBName, UserDislikeCommentCollection)) {
                arango.createCollection(arangoDB, DBName, UserDislikeCommentCollection, true);
            }

            String edgeKey = userId+"/"+commentId;
            // if user already dislikes the comment, then remove his dislike and update dislike count
            if(arango.documentExists(arangoDB, DBName, UserDislikeCommentCollection,edgeKey)){
                arango.deleteDocument(arangoDB, DBName, UserDislikeCommentCollection, edgeKey);

                BaseDocument originalComment = arango.readDocument(arangoDB, DBName, CommentCollectionName, commentId);
                int newDisikes =  (int)originalComment.getAttribute(DISLIKES)-1;
                originalComment.updateAttribute(LIKES,newDisikes);
                // putting the comment with the updated amount of dislikes
                arango.updateDocument(arangoDB,DBName,CommentCollectionName,originalComment,commentId);

                msg = "removed your dislike on the comment";
            }
            else { // then user wants to dilike this comment, so we create an edge and update the number of dilikes
                msg = "added your dislike on the comment";

                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom("Users/" + userId);
                edgeDocument.setTo("Comments/" + commentId);

                // adding new edgeDocument representing that a user dislikes a comment
                arango.createEdgeDocument(arangoDB, DBName, UserDislikeCommentCollection, edgeDocument);

                // retrieving the original comment with the old amount of likes
                BaseDocument originalComment = arango.readDocument(arangoDB, DBName, CommentCollectionName, commentId);
                int newDislikes = (int) originalComment.getAttribute(DISLIKES)+1;
                int newLikes = (int) originalComment.getAttribute(LIKES);
                //checking if the user likes this content to remove his like
                if (arango.documentExists(arangoDB, DBName, UserLikeCommentCollection, edgeKey)) {
                    arango.deleteDocument(arangoDB, DBName, UserLikeCommentCollection, edgeKey);
                    newLikes -= 1;
                    msg += " & removed your like";
                }
                originalComment.updateAttribute(LIKES, newLikes);
                originalComment.updateAttribute(LIKES, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(arangoDB, DBName, CommentCollectionName, originalComment, commentId);
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }




    public static void main(String[] args) {
        DislikeComment dc = new DislikeComment();
        JSONObject request = new JSONObject("{\"body\":{\"commentId\":\"1998-2-9\"},\"uriParams\":{\"userId\":\"asdasda\"},\"methodType\":\"PUT\"}");
        System.out.println(dc.execute(request));
    }
}
