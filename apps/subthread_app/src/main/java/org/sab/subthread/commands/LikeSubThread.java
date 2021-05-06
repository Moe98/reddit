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

            String edgeKey = userId+"/"+subthreadId;
            // if user already likes the subthread, then remove his like and update like count
            if(arango.documentExists(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME,edgeKey)){
                arango.deleteDocument(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, edgeKey);

                BaseDocument originalSubthread = arango.readDocument(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newLikes =  (int)originalSubthread.getAttribute(LIKES)-1;
                originalSubthread.updateAttribute(LIKES,newLikes);
                // putting the comment with the updated amount of likes
                arango.updateDocument(arangoDB, DB_Name,SUBTHREAD_COLLECTION_NAME,originalSubthread,subthreadId);

                msg = "removed your like on the subthread";
            }
            else { // then user wants to like this subthread, so we create an edge and update the number of likes
                msg = "added your like on the subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

                // adding new edgeDocument representing that a user likes a comment
                arango.createEdgeDocument(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of likes and dislikes
                BaseDocument originalSubthread = arango.readDocument(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newLikes = (int) originalSubthread.getAttribute(LIKES) + 1;
                int newDislikes = (int) originalSubthread.getAttribute(DISLIKES);
                //checking if the user dislikes this subthread to remove his dislike
                if (arango.documentExists(arangoDB, DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, edgeKey)) {
                    arango.deleteDocument(arangoDB, DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, edgeKey);
                    newDislikes -= 1;
                    msg += " & removed your dislike";
                }
                originalSubthread.updateAttribute(LIKES, newLikes);
                originalSubthread.updateAttribute(LIKES, newDislikes);
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
}
