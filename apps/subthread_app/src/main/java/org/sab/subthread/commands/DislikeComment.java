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

    @Override
    public String execute() {
        String commentId = body.getString(COMMENT_ID);
        String userId = uriParams.getString(ACTION_MAKER_ID);

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, COMMENT_COLLECTION_NAME)) {
                // TODO if this doesn't exist something is wrong!
                arango.createCollection(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, true);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, true);
            }

            // TODO check user exists
            // TODO why not let it autogenerate a key?
            String edgeKey = userId+"-"+commentId;
            // if user already dislikes the comment, then remove his dislike and update dislike count
            if(arango.documentExists(arangoDB, DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME,edgeKey)){
                arango.deleteDocument(arangoDB, DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, edgeKey);

                BaseDocument originalComment = arango.readDocument(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, commentId);
                // TODO make this thread safe
                //  I feel like this var is unnecessary to begin with
                int newDisikes =  (int)originalComment.getAttribute(DISLIKES_DB)-1;
                originalComment.updateAttribute(DISLIKES_DB,newDisikes);
                // putting the comment with the updated amount of dislikes
                arango.updateDocument(arangoDB,DB_Name,COMMENT_COLLECTION_NAME,originalComment,commentId);

                msg = "removed your dislike on the comment";
            }
            else { // then user wants to dilike this comment, so we create an edge and update the number of dilikes
                msg = "added your dislike on the comment";

                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(COMMENT_COLLECTION_NAME+ "/" + commentId);

                // adding new edgeDocument representing that a user dislikes a comment
                arango.createEdgeDocument(arangoDB, DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of likes
                BaseDocument originalComment = arango.readDocument(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, commentId);
                int newDislikes = (int) originalComment.getAttribute(DISLIKES_DB)+1;
                // TODO why update likes?
                int newLikes = (int) originalComment.getAttribute(LIKES_DB);
                //checking if the user likes this content to remove his like
                if (arango.documentExists(arangoDB, DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, edgeKey)) {
                    arango.deleteDocument(arangoDB, DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, edgeKey);
                    newLikes -= 1;
                    msg += " & removed your like";
                }
                originalComment.updateAttribute(LIKES_DB, newLikes);
                originalComment.updateAttribute(DISLIKES_DB, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, originalComment, commentId);
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }

    public static void main(String[] args) {
        DislikeComment dc = new DislikeComment();
        JSONObject request = new JSONObject("{\"body\":{\"commentId\":\"21289\"},\"uriParams\":{\"userId\":\"asdafsda\"},\"methodType\":\"PUT\"}");
        System.out.println(dc.execute(request));
    }
}
