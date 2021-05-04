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

public class DislikeSubthread extends SubThreadCommand{

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
            // if user already dislikes the subthread, then remove his dislike and update dislike count
            if(arango.documentExists(arangoDB, DBName, UserDislikeSubthreadCollection,edgeKey)){
                arango.deleteDocument(arangoDB, DBName, UserDislikeSubthreadCollection, edgeKey);

                BaseDocument originalComment = arango.readDocument(arangoDB, DBName, SubthreadCollectionName, subthreadId);
                int newDisikes =  (int)originalComment.getAttribute(DISLIKES)-1;
                originalComment.updateAttribute(LIKES,newDisikes);
                // putting the comment with the updated amount of dislikes
                arango.updateDocument(arangoDB,DBName,SubthreadCollectionName,originalComment,subthreadId);

                msg = "removed your dislike on the subthread";
            }
            else { // then user wants to dislike this subthread, so we create an edge and update the number of dislikes
                msg = "added your dislike on the subthread";

                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom("Users/" + userId);
                edgeDocument.setTo("Threads/" + subthreadId);

                // adding new edgeDocument representing that a user dislikes a subthread
                arango.createEdgeDocument(arangoDB, DBName, UserDislikeSubthreadCollection, edgeDocument);

                // retrieving the original comment with the old amount of dislikes and likes
                BaseDocument originalSubthread = arango.readDocument(arangoDB, DBName, SubthreadCollectionName, subthreadId);
                int newDislikes = (int) originalSubthread.getAttribute(DISLIKES)+1;
                int newLikes = (int) originalSubthread.getAttribute(LIKES);
                //checking if the user likes this subthread to remove his like
                if (arango.documentExists(arangoDB, DBName, UserLikeSubthreadCollection, edgeKey)) {
                    arango.deleteDocument(arangoDB, DBName, UserLikeSubthreadCollection, edgeKey);
                    newLikes -= 1;
                    msg += " & removed your like";
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
