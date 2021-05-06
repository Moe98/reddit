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
    private String SubThreadCollectionName;
    private String UserBookmarkSubThreadCollection;
    private String DBName;

    @Override
    protected String execute() {

        SubThreadCollectionName = "Subthread";
        UserBookmarkSubThreadCollection = "UserBookmarkSubthread";

        DBName = "ARANGO_DB";

        JSONObject response = new JSONObject();
        String msg = "";

        try {

            String subthreadId = body.getString(SUBTHREAD_ID);
            String userId = uriParams.getString(USER_ID);

            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, SubThreadCollectionName)) {
                // TODO if this doesn't exist something is wrong!
                arango.createCollection(arangoDB, DBName, SubThreadCollectionName, false);
            }
            if (!arango.collectionExists(arangoDB, DBName, UserBookmarkSubThreadCollection)) {
                arango.createCollection(arangoDB, DBName, UserBookmarkSubThreadCollection, true);
            }

            String edgeKey = userId + subthreadId;

            if(arango.documentExists(arangoDB, DBName, UserBookmarkSubThreadCollection, edgeKey)){
                msg = "Removed Subthread from Bookmarks";
                // unbookmark
                arango.deleteDocument(arangoDB, DBName, UserBookmarkSubThreadCollection, edgeKey);

            } else {
                // bookmark
                msg = "Bookmarked Subthread";
                BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
                edgeDocument.setKey(edgeKey);
                edgeDocument.setFrom("User/" + userId);
                edgeDocument.setTo("Subthread/" + subthreadId);
                arango.createEdgeDocument(arangoDB, DBName, UserBookmarkSubThreadCollection, edgeDocument);

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
