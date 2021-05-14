package org.sab.thread.commands;

import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class AssignThreadModerator extends ThreadCommand {

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

    @Override
    protected Schema getSchema() {
        Attribute parentSubthreadId = new Attribute(THREAD_NAME, DataType.STRING, true);
        Attribute newModeratorId = new Attribute(MODERATOR_ID, DataType.STRING, true);

        return new Schema(List.of(parentSubthreadId, newModeratorId));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {

        Arango arango = null;
        JSONObject response = new JSONObject();
        String msg = "";

        try {

            String threadId = body.getString(THREAD_NAME);
            String modId = body.getString(MODERATOR_ID);

            String assignerId = uriParams.getString(ASSIGNER_ID);

            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            if (!arango.collectionExists(DB_Name, THREAD_COLLECTION_NAME)) {
                // TODO if this doesn't exist something is wrong!
                arango.createCollection(DB_Name, THREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_MOD_THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_MOD_THREAD_COLLECTION_NAME, true);
            }

            // check if thread exists
            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadId)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // check if assigner is a moderator on this thread
            String assignerModEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_MOD_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + assignerId,
                    THREAD_COLLECTION_NAME + "/" + threadId);
            if (assignerModEdgeId.equals("")) {
                // assigner is not a mod
                msg = "You don't have permission to assign a moderator for this thread";
                return Responder.makeErrorResponse(msg, 401).toString();
            }

            String moderatorModEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_MOD_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + modId,
                    THREAD_COLLECTION_NAME + "/" + threadId);

            if (!moderatorModEdgeId.equals("")) {
                msg = "User already moderates this thread";
                return Responder.makeErrorResponse(msg, 400).toString();

            } else {
                // bookmark
                msg = "Assigned Moderator";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + modId);
                edgeDocument.setTo(THREAD_COLLECTION_NAME + "/" + threadId);
                arango.createEdgeDocument(DB_Name, USER_MOD_THREAD_COLLECTION_NAME, edgeDocument);

            }

        } catch (Exception e) {
//            System.out.println(e.getStackTrace());
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();

        } finally {
            if (arango != null) {
                arango.disconnect();
            }
            arango.disconnect();
            response.put("msg", msg);
        }

        return Responder.makeDataResponse(response).toString();

    }

}
