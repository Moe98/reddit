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

            String edgeKey = userId+commentId;
            // if user already likes the comment, then remove his like and update like count
            if(arango.documentExists(arangoDB, DBName, UserLikeCommentCollection,edgeKey)){
                arango.deleteDocument(arangoDB, DBName, UserLikeCommentCollection, edgeKey);

                BaseDocument originalComment = arango.readDocument(arangoDB, DBName, CommentCollectionName, commentId);
                int newLikes =  (int)originalComment.getAttribute(LIKES)-1;
                originalComment.updateAttribute(LIKES,newLikes);
                // putting the comment with the updated amount of likes
                arango.updateDocument(arangoDB,DBName,CommentCollectionName,originalComment,commentId);

                msg = "removed your like on the comment";
            }
            else { // then user wants to like this comment, so we create an edge and update the number of likes
                msg = "added your like on the comment";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom("Users/" + userId);
                edgeDocument.setTo("Comments/" + commentId);

                // adding new edgeDocument representing that a user likes a comment
                arango.createEdgeDocument(arangoDB, DBName, UserLikeCommentCollection, edgeDocument);

                // retrieving the original comment with the old amount of likes
                BaseDocument originalComment = arango.readDocument(arangoDB, DBName, CommentCollectionName, commentId);
                int newLikes = (int) originalComment.getAttribute(LIKES) + 1;
                int newDislikes = (int) originalComment.getAttribute(DISLIKES);
                //checking if the user dislikes this content to remove his dislike
                if (arango.documentExists(arangoDB, DBName, UserDislikeCommentCollection, edgeKey)) {
                    arango.deleteDocument(arangoDB, DBName, UserDislikeCommentCollection, edgeKey);
                    newDislikes -= 1;
                    msg += " & removed your dislike";
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
        LikeComment lc = new LikeComment();
        JSONObject request = new JSONObject("{\"body\":{\"commentId\":\"1998-2-9\"},\"uriParams\":{\"userId\":\"asdasda\"},\"methodType\":\"PUT\"}");
        System.out.println(lc.execute(request));
    }
}
