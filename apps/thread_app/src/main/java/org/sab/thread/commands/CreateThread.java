package org.sab.thread.commands;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.sab.arango.Arango;
import org.sab.models.Thread;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class CreateThread extends ThreadCommand {
    private final long INITIAL_NUM_FOLLOWERS = 0;

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected Schema getSchema() {
        Attribute threadName = new Attribute(THREAD_NAME, DataType.STRING, true);
        Attribute description = new Attribute(DESCRIPTION, DataType.STRING, true);
        Attribute numOfFollowers = new Attribute(NUM_OF_FOLLOWERS, DataType.INT);

        return new Schema(List.of(threadName, description, numOfFollowers));
    }

    // private Arango arango;
    // private ArangoDB arangoDB;

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    public String execute() {

        Arango arango = null;

        Thread thread = new Thread();

        try {
            String name = body.getString(THREAD_NAME);
            String description = body.getString(DESCRIPTION);
            String creatorId = authenticationParams.getString(ThreadCommand.USERNAME);

            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, USER_COLLECTION_NAME, false);

            arango.createCollectionIfNotExists(DB_Name, THREAD_COLLECTION_NAME, false);

            arango.createCollectionIfNotExists(DB_Name, USER_MOD_THREAD_COLLECTION_NAME, true);

            if (!arango.documentExists(DB_Name, USER_COLLECTION_NAME, creatorId)) {
                return Responder.makeErrorResponse(OBJECT_NOT_FOUND, 404);
            }
            // TODO can we do this as a transaction?

            BaseDocument myObject = new BaseDocument();
            myObject.setKey(name);
//            myObject.addAttribute(THREAD_NAME_DB, name);
            myObject.addAttribute(DESCRIPTION_DB, description);
            myObject.addAttribute(CREATOR_ID_DB, creatorId);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            myObject.addAttribute(DATE_CREATED_DB, sqlDate);
            myObject.addAttribute(NUM_OF_FOLLOWERS_DB, INITIAL_NUM_FOLLOWERS);

            BaseDocument res = arango.createDocument(DB_Name, THREAD_COLLECTION_NAME, myObject);

            thread.setName(res.getKey());
            thread.setDescription((String) res.getAttribute(DESCRIPTION_DB));
            thread.setCreatorId((String) res.getAttribute(CREATOR_ID_DB));
            thread.setDateCreated((String) res.getAttribute(DATE_CREATED_DB));
            thread.setNumOfFollowers(Integer.parseInt(String.valueOf(res.getAttribute(NUM_OF_FOLLOWERS_DB))));

            // assign creator to be mod
            BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
            edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + creatorId);
            edgeDocument.setTo(THREAD_COLLECTION_NAME + "/" + name);
            arango.createEdgeDocument(DB_Name, USER_MOD_THREAD_COLLECTION_NAME, edgeDocument);


        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        } finally {
            if (arango != null) {

            }
        }
        return Responder.makeDataResponse(thread.toJSON());
    }

}
