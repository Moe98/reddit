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

            // TODO check subthread and user exist

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME)) {
                // TODO if this doesn't exist something is wrong!
                arango.createCollection(arangoDB, DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, true);
            }

            String edgeKey = userId + subthreadId;

            if(arango.documentExists(arangoDB, DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, edgeKey)){
                msg = "Removed Subthread from Bookmarks";
                // unbookmark
                arango.deleteDocument(arangoDB, DB_Name, USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME, edgeKey);

            } else {
                // bookmark
                msg = "Bookmarked Subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
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

//    public static void main(String[] args) {
//        String query = """
//                            let arr = (
//                            FOR v IN 1..1 OUTBOUND "User/%s" UserDislikeComment
//                                FILTER v._id == "Comment/%s"
//                                RETURN DISTINCT v._key
//                            )
//
//                            return LENGTH(arr)""".formatted("33366", "44494");
//        System.out.println(query);
//        String DBName = "ARANGO_DB";
//        Arango arango = Arango.getInstance();
//        ArangoDB arangoDB = arango.connect();
//
//        ArangoCursor<BaseDocument> cursor = arango.query(arangoDB,DBName, query, null);
//        BaseDocument baseDoc;
//        System.out.println(cursor.hasNext());
//        while (cursor.hasNext()) {
//            baseDoc = cursor.next();
////            Map <String, Object> map = baseDoc.getProperties();
////            for(Map.Entry<String, Object> entry : map.entrySet()) {
////
////                System.out.println(entry.getKey() + ": " + entry.getValue());
////            }
//
//        }
//
//
//    }

   public static void main(String[] args) {
       BookmarkSubThread tc = new BookmarkSubThread();

       JSONObject body = new JSONObject();
       body.put("Id", "32750");

       JSONObject uriParams = new JSONObject();
       uriParams.put("userId", "32930");

       JSONObject request = new JSONObject();
       request.put("body", body);
       request.put("methodType", "POST");
       request.put("uriParams", uriParams);

       System.out.println(request);
       System.out.println("----------");

       System.out.println(tc.execute(request));
   }

}
