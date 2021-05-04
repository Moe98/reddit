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

    protected Schema getSchema() {
        Attribute parentSubthreadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        return new Schema(List.of(parentSubthreadId));
    }

    private Arango arango;
    private ArangoDB arangoDB;
    private String SubthreadCollectionName;
    private String UserLikeSubthreadCollection;
    private String UserDislikeSubthreadCollection;
    private String DBName;

    @Override
    public String execute() {
        String subthreadId = body.getString(SUBTHREAD_ID);
        String userId = uriParams.getString(USER_ID);

        SubthreadCollectionName = "Subthread";
        UserLikeSubthreadCollection = "UserLikeSubthread";
        UserDislikeSubthreadCollection = "UserDislikeSubthread";
        DBName = "ARANGO_DB";

        JSONObject response = new JSONObject();
        String msg = "";
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, SubthreadCollectionName)) {
                arango.createCollection(arangoDB, DBName, SubthreadCollectionName, false);
            }
            if (!arango.collectionExists(arangoDB, DBName, UserLikeSubthreadCollection)) {
                arango.createCollection(arangoDB, DBName, UserLikeSubthreadCollection, true);
            }
            if (!arango.collectionExists(arangoDB, DBName, UserDislikeSubthreadCollection)) {
                arango.createCollection(arangoDB, DBName, UserDislikeSubthreadCollection, true);
            }

            String edgeKey = userId+subthreadId;
            // if user already likes the subthread, then remove his like and update like count
            if(arango.documentExists(arangoDB, DBName, UserLikeSubthreadCollection,edgeKey)){
                arango.deleteDocument(arangoDB, DBName, UserLikeSubthreadCollection, edgeKey);

                BaseDocument originalComment = arango.readDocument(arangoDB, DBName, SubthreadCollectionName, subthreadId);
                int newLikes =  (int)originalComment.getAttribute(LIKES)-1;
                originalComment.updateAttribute(LIKES,newLikes);
                // putting the comment with the updated amount of likes
                arango.updateDocument(arangoDB,DBName,SubthreadCollectionName,originalComment,subthreadId);

                msg = "removed your like on the subthread";
            }
            else { // then user wants to like this subthread, so we create an edge and update the number of likes
                msg = "added your like on the subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom("Users/" + userId);
                edgeDocument.setTo("Threads/" + subthreadId);

                // adding new edgeDocument representing that a user likes a comment
                arango.createEdgeDocument(arangoDB, DBName, UserLikeSubthreadCollection, edgeDocument);

                // retrieving the original comment with the old amount of likes and dislikes
                BaseDocument originalSubthread = arango.readDocument(arangoDB, DBName, SubthreadCollectionName, subthreadId);
                int newLikes = (int) originalSubthread.getAttribute(LIKES) + 1;
                int newDislikes = (int) originalSubthread.getAttribute(DISLIKES);
                //checking if the user dislikes this subthread to remove his dislike
                if (arango.documentExists(arangoDB, DBName, UserDislikeSubthreadCollection, edgeKey)) {
                    arango.deleteDocument(arangoDB, DBName, UserDislikeSubthreadCollection, edgeKey);
                    newDislikes -= 1;
                    msg += " & removed your dislike";
                }
                originalSubthread.updateAttribute(LIKES, newLikes);
                originalSubthread.updateAttribute(LIKES, newDislikes);
                // putting the comment with the updated amount of likes and dislikes
                arango.updateDocument(arangoDB, DBName, SubthreadCollectionName, originalSubthread, subthreadId);
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
