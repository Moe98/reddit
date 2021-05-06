package org.sab.thread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class AssignThreadModerator extends ThreadCommand {

    @Override
    protected Schema getSchema() {
        Attribute parentSubthreadId = new Attribute(THREAD_NAME, DataType.STRING, true);
        Attribute newModeratorId = new Attribute(MODERATOR_ID, DataType.STRING, true);

        return new Schema(List.of(parentSubthreadId, newModeratorId));
    }

    private Arango arango;
    private ArangoDB arangoDB;
    private String ThreadCollectionName;
    private String UserCollectionName;
    private String UserThreadModCollection;
    private String DBName;

    @Override
    protected String execute() {
        ThreadCollectionName = "Thread";
        UserCollectionName = "User";
        UserThreadModCollection = "UserThreadMod";

        DBName = "ARANGO_DB";

        JSONObject response = new JSONObject();
        String msg = "";

        try {

            String threadId = body.getString(THREAD_NAME);
            String modId = body.getString(MODERATOR_ID);

            String assignerId = uriParams.getString(ASSIGNER_ID);

            arango = Arango.getInstance();
            arangoDB = arango.connect();
            if (!arango.collectionExists(arangoDB, DBName, ThreadCollectionName)) {
                // TODO if this doesn't exist something is wrong!
                arango.createCollection(arangoDB, DBName, ThreadCollectionName, false);
            }
            if (!arango.collectionExists(arangoDB, DBName, UserCollectionName)) {
                arango.createCollection(arangoDB, DBName, UserCollectionName, true);
            }
            if (!arango.collectionExists(arangoDB, DBName, UserThreadModCollection)) {
                arango.createCollection(arangoDB, DBName, UserThreadModCollection, true);
            }

            // TODO check if assigner, mod and thread exist
            // TODO check if assigner is the current mod
            //  Can there be more than one mod?


            String edgeKey = modId + threadId;

            if (arango.documentExists(arangoDB, DBName, UserThreadModCollection, edgeKey)) {
                msg = "User already moderates this thread";
                return Responder.makeErrorResponse(msg, 404).toString();
                // TODO error


            } else {
                // bookmark
                msg = "Assigned Moderator";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom("User/" + modId);
                edgeDocument.setTo("Subthread/" + threadId);
                arango.createEdgeDocument(arangoDB, DBName, UserThreadModCollection, edgeDocument);

            }

        } catch (Exception e) {
//            System.out.println(e.getStackTrace());
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();

        } finally {
            arango.disconnect(arangoDB);
            response.put("msg", msg);
        }

        return Responder.makeDataResponse(response).toString();

    }

    public static void main(String[] args) {
        AssignThreadModerator tc = new AssignThreadModerator();

        JSONObject body = new JSONObject();
        body.put(THREAD_NAME, "asmakElRayes7amido");
        body.put(MODERATOR_ID, "33366");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ASSIGNER_ID, "32930");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(tc.execute(request));
    }

}
