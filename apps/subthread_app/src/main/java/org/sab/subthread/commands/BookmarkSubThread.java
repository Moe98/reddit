package org.sab.subthread.commands;

import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.NotificationMessages;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.notifyApp;

public class BookmarkSubThread extends SubThreadCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected Schema getSchema() {
        Attribute subthreadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        return new Schema(List.of(subthreadId));
    }

    @Override
    protected String execute() {

        Arango arango;

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            String subthreadId = body.getString(SUBTHREAD_ID);
            String userId = authenticationParams.getString(USERNAME);

            arango = Arango.getInstance();

            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            arango.createCollectionIfNotExists(DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, true);


            if (!existsInCouchbase(subthreadId) && !existsInArango(SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400);
            }


            String userBookmarkEdgeId = arango.getSingleEdgeId(DB_Name,
                    USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + userId,
                    SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

            if (!userBookmarkEdgeId.equals("")) {
                msg = "Removed Subthread from Bookmarks";
                // unbookmark
                arango.deleteDocument(DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, userBookmarkEdgeId);

            } else {
                // bookmark
                msg = "Bookmarked Subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);
                arango.createEdgeDocument(DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, edgeDocument);
                // notify the user who is bookmarking with the bookmarking
                notifyApp(Notification_Queue_Name, NotificationMessages.SUBTHREAD_BOOKMARK_MSG.getMSG(), subthreadId, userId, SEND_NOTIFICATION_FUNCTION_NAME);
            }


        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);

        }

        return Responder.makeDataResponse(response);

    }

}
