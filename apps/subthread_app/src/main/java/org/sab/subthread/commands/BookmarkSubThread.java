package org.sab.subthread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class BookmarkSubThread extends SubThreadCommand {

    @Override
    protected Schema getSchema() {
        Attribute subthreadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        return new Schema(List.of(subthreadId));
    }

    private Arango arango;
    private ArangoDB arangoDB;


    @Override
    protected String execute() {

        JSONObject response = new JSONObject();
        String msg = "";

        try {

            String subthreadId = body.getString(SUBTHREAD_ID);
            String userId = uriParams.getString(REPORTER_ID);

            arango = Arango.getInstance();
            arangoDB = arango.connect();


            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME)) {
                // TODO if this doesn't exist something is wrong!
                arango.createCollection(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, true);
            }

            // check subthread exist
            if(!arango.documentExists(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            String userBookmarkEdgeId = arango.getSingleEdgeId(arangoDB,
                                                                DB_Name,
                                                                USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME,
                                                                USER_COLLECTION_NAME + "/" + userId,
                                                                SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);

            if(!userBookmarkEdgeId.equals("")){
                msg = "Removed Subthread from Bookmarks";
                // unbookmark
                arango.deleteDocument(arangoDB, DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, userBookmarkEdgeId);

            } else {
                // bookmark
                msg = "Bookmarked Subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(USER_COLLECTION_NAME + "/" + userId);
                edgeDocument.setTo(SUBTHREAD_COLLECTION_NAME + "/" + subthreadId);
                arango.createEdgeDocument(arangoDB, DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, edgeDocument);

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
       BookmarkSubThread tc = new BookmarkSubThread();

       JSONObject body = new JSONObject();
       body.put("id", "126209");

       JSONObject uriParams = new JSONObject();
       uriParams.put("userId", "33366");

       JSONObject request = new JSONObject();
       request.put("body", body);
       request.put("methodType", "POST");
       request.put("uriParams", uriParams);

       System.out.println(request);
       System.out.println("----------");

       System.out.println(tc.execute(request));
   }

}
