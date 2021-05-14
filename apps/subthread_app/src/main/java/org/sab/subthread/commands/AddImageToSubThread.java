package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.minio.MinIO;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.List;

public class AddImageToSubThread extends SubThreadCommand {
    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {
        // JSONObject response = new JSONObject();
        String msg = "";

        Arango arango = null;

        try {
//            boolean authenticated = authenticationParams.getBoolean(IS_AUTHENTICATED);
//            if (!authenticated)
//                return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);
//            if (files.length() != 1) {
//                return Responder.makeErrorResponse("Only one profile image allowed per upload, Check Form-Data Files!", 400);
//            }

            // retrieving the body objects
            String userId = authenticationParams.getString(USERNAME);
            String subthreadId = uriParams.getString(SUBTHREAD_ID);
            String output;

            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            // check subthread exist
            if (!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            final BaseDocument subthreadDocument = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
            subthreadDocument.updateAttribute(HASIMAGE_DB, true);

            final String creatorId = (String) subthreadDocument.getAttribute(CREATOR_ID_DB);
            // check that the user is the creator
            if (!userId.equals(creatorId)) {
                return Responder.makeErrorResponse(REQUESTER_NOT_AUTHOR, 403).toString();
            }

            String publicId = subthreadId.replaceAll("[-]", "");

            output = MinIO.uploadObject(BUCKETNAME, publicId, files.getJSONObject("image"));
            if (output.isEmpty()) {
                return Responder.makeErrorResponse("Error Occurred While Uploading Your Image!", 404);
            }

            arango.updateDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadDocument, subthreadId);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
        }

        return Responder.makeMsgResponse("Profile Picture uploaded successfully");
    }
}
