package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikeComment extends CommentCommand{
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
                arango.createCollection(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, true);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, true);
            }

            String likeEdgeId = arango.getSingleEdgeId(arangoDB,DB_Name,USER_LIKE_COMMENT_COLLECTION_NAME,USER_COLLECTION_NAME+"/"+userId,COMMENT_COLLECTION_NAME+"/"+commentId);

            // if user already likes the comment, then remove his like and update like count
            if(!likeEdgeId.equals("")){
                arango.deleteDocument(arangoDB, DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, likeEdgeId);

                BaseDocument originalComment = arango.readDocument(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, commentId);
                int newLikes =  (int)originalComment.getAttribute(LIKES_DB)-1;
                originalComment.updateAttribute(LIKES_DB,newLikes);
                // putting the comment with the updated amount of likes
                arango.updateDocument(arangoDB,DB_Name,COMMENT_COLLECTION_NAME,originalComment,commentId);

                msg = "removed your like on the comment";
            }
            else { // then user wants to like this comment, so we create an edge and update the number of likes
                msg = "added your like on the comment";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(COMMENT_COLLECTION_NAME+ "/" + commentId);

                // adding new edgeDocument representing that a user likes a comment
                arango.createEdgeDocument(arangoDB, DB_Name, USER_LIKE_COMMENT_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of likes
                BaseDocument originalComment = arango.readDocument(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, commentId);
                int newLikes = (int) originalComment.getAttribute(LIKES_DB) + 1;
                int newDislikes = (int) originalComment.getAttribute(DISLIKES_DB);
                //checking if the user dislikes this content to remove his dislike
                String dislikeEdgeId = arango.getSingleEdgeId(arangoDB,DB_Name,USER_DISLIKE_COMMENT_COLLECTION_NAME,USER_COLLECTION_NAME+"/"+userId,COMMENT_COLLECTION_NAME+"/"+commentId);
                if (!dislikeEdgeId.equals("")) {
                    arango.deleteDocument(arangoDB, DB_Name, USER_DISLIKE_COMMENT_COLLECTION_NAME, dislikeEdgeId);
                    newDislikes -= 1;
                    msg += " & removed your dislike";
                }
                originalComment.updateAttribute(LIKES_DB, newLikes);
                originalComment.updateAttribute(DISLIKES_DB, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(arangoDB, DB_Name, COMMENT_COLLECTION_NAME, originalComment, commentId);
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
//        JSONObject request = new JSONObject("{\"body\":{\"commentId\":\"21289\"},\"uriParams\":{\"userId\":\"asdafsda\"},\"methodType\":\"PUT\"}");


        JSONObject body = new JSONObject();
        body.put(COMMENT_ID, "21289");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "asdafsda");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(lc.execute(request));
    }
}
