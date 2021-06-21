package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.minio.MinIO;
import org.sab.models.CouchbaseBuckets;
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
        String msg;

        Arango arango;

        try {
            // retrieving the body objects
            String userId = authenticationParams.getString(USERNAME);
            String subthreadId = uriParams.getString(SUBTHREAD_ID);
            String output;

            arango = Arango.getInstance();

            // check subthread exist
            if (!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400);
            }

            final BaseDocument subthreadDocument = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
            subthreadDocument.updateAttribute(HAS_IMAGE_DB, true);

            final String creatorId = (String) subthreadDocument.getAttribute(CREATOR_ID_DB);
            // check that the user is the creator
            if (!userId.equals(creatorId)) {
                return Responder.makeErrorResponse(REQUESTER_NOT_AUTHOR, 403);
            }

            String publicId = subthreadId.replaceAll("[-]", "");

            output = MinIO.uploadObject(BUCKETNAME, publicId, files.getJSONObject("image"));
            if (output.isEmpty()) {
                return Responder.makeErrorResponse("Error Occurred While Uploading Your Image!", 404);
            }

            arango.updateDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadDocument, subthreadId);

            final BaseDocument updatedSubThreadDocument = arango.readDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId);
            final JSONObject object = baseDocumentToJson(updatedSubThreadDocument);
            Couchbase.getInstance().replaceDocument(CouchbaseBuckets.RECOMMENDED_SUB_THREADS.get(),
                    updatedSubThreadDocument.getKey(), object);
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeMsgResponse("Profile Picture uploaded successfully");
    }
}
