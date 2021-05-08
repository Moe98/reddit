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

public class LikeSubThread extends SubThreadCommand{
    @Override
    protected Schema getSchema() {
        Attribute subthreadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        return new Schema(List.of(subthreadId));
    }

    private Arango arango;
    private ArangoDB arangoDB;

    @Override
    public String execute() {
        String subthreadId = body.getString(SUBTHREAD_ID);
        String userId = uriParams.getString(REPORTER_ID);
        JSONObject response = new JSONObject();
        String msg = "";
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, true);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, true);
            }

            // TODO check if subthread exists
            if(!arango.documentExists(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            String likeEdgeId = arango.getSingleEdgeId(arangoDB,DB_Name,USER_LIKE_SUBTHREAD_COLLECTION_NAME,USER_COLLECTION_NAME+"/"+userId,SUBTHREAD_COLLECTION_NAME+"/"+subthreadId);

            // if user already likes the subthread, then remove his like and update like count
            if(!likeEdgeId.equals("")){
                arango.deleteDocument(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, likeEdgeId);

                BaseDocument originalSubthread = arango.readDocument(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newLikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(LIKES_DB)))-1;
                originalSubthread.updateAttribute(LIKES_DB,newLikes);
                // putting the comment with the updated amount of likes
                arango.updateDocument(arangoDB, DB_Name,SUBTHREAD_COLLECTION_NAME,originalSubthread,subthreadId);

                msg = "removed your like on the subthread";
            }
            else { // then user wants to like this subthread, so we create an edge and update the number of likes
                msg = "added your like on the subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

                // adding new edgeDocument representing that a user likes a comment
                arango.createEdgeDocument(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of likes and dislikes
                BaseDocument originalSubthread = arango.readDocument(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newLikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(LIKES_DB))) + 1;
                int newDislikes = Integer.parseInt(String.valueOf(originalSubthread.getAttribute(DISLIKES_DB)));

                String dislikeEdgeId = arango.getSingleEdgeId(arangoDB,DB_Name,USER_DISLIKE_SUBTHREAD_COLLECTION_NAME,USER_COLLECTION_NAME+"/"+userId,SUBTHREAD_COLLECTION_NAME+"/"+subthreadId);
                //checking if the user dislikes this subthread to remove his dislike
                if (!dislikeEdgeId.equals("")) {
                    arango.deleteDocument(arangoDB, DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, dislikeEdgeId);
                    newDislikes -= 1;
                    msg += " & removed your dislike";
                }
                originalSubthread.updateAttribute(LIKES_DB, newLikes);
                originalSubthread.updateAttribute(DISLIKES_DB, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, originalSubthread, subthreadId);
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
        LikeSubThread tc = new LikeSubThread();

        JSONObject body = new JSONObject();
        body.put(SUBTHREAD_ID, "126033");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "asdafsda");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(tc.execute(request));
    }
}
