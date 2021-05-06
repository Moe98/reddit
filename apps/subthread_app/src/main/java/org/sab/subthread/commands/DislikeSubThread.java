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

public class DislikeSubThread extends SubThreadCommand{
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

            String edgeKey = userId+"-"+subthreadId;
            // if user already dislikes the subthread, then remove his dislike and update dislike count
            if(arango.documentExists(arangoDB, DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME,edgeKey)){
                arango.deleteDocument(arangoDB, DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, edgeKey);

                BaseDocument originalSubthread = arango.readDocument(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newDisikes =  (int)originalSubthread.getAttribute(DISLIKES_DB)-1;
                originalSubthread.updateAttribute(DISLIKES_DB,newDisikes);
                // putting the comment with the updated amount of dislikes
                arango.updateDocument(arangoDB, DB_Name,SUBTHREAD_COLLECTION_NAME,originalSubthread,subthreadId);

                msg = "removed your dislike on the subthread";
            }
            else { // then user wants to dislike this subthread, so we create an edge and update the number of dislikes
                msg = "added your dislike on the subthread";

                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom("Users/" + userId);
                edgeDocument.setTo("SubThreads/" + subthreadId);

                // adding new edgeDocument representing that a user dislikes a subthread
                arango.createEdgeDocument(arangoDB, DB_Name, USER_DISLIKE_SUBTHREAD_COLLECTION_NAME, edgeDocument);

                // retrieving the original comment with the old amount of dislikes and likes
                BaseDocument originalSubthread = arango.readDocument(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
                int newDislikes = (int) originalSubthread.getAttribute(DISLIKES_DB)+1;
                int newLikes = (int) originalSubthread.getAttribute(LIKES_DB);
                //checking if the user likes this subthread to remove his like
                if (arango.documentExists(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, edgeKey)) {
                    arango.deleteDocument(arangoDB, DB_Name, USER_LIKE_SUBTHREAD_COLLECTION_NAME, edgeKey);
                    newLikes -= 1;
                    msg += " & removed your like";
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
        DislikeSubThread tc = new DislikeSubThread();

        JSONObject body = new JSONObject();
        body.put(SUBTHREAD_ID, "16871");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "67890");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(tc.execute(request));
    }
}
