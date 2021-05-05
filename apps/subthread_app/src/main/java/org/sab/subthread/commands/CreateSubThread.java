package org.sab.subthread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.SubThread;
import org.sab.models.Thread;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.Date;
import java.util.List;

public class CreateSubThread extends SubThreadCommand {
    final long INITIAL_LIKES = 0;
    final long INITIAL_DISLIKES = 0;

    @Override
    protected Schema getSchema() {
        Attribute parentThreadId = new Attribute(PARENT_THREAD_ID, DataType.STRING, true);
        Attribute creatorId = new Attribute(CREATOR_ID, DataType.STRING, true);

        Attribute title = new Attribute(TITLE, DataType.STRING, true);
        Attribute content = new Attribute(CONTENT, DataType.STRING, true);

        Attribute hasImage = new Attribute(HASIMAGE, DataType.BOOLEAN, true);

        return new Schema(List.of(parentThreadId, creatorId, title, content, hasImage));
    }

    private Arango arango;
    private ArangoDB arangoDB;
    private String collectionName;
    private String DBName;

    @Override
    protected String execute() {
        String parentThreadId = body.getString(PARENT_THREAD_ID);
        String creatorId = body.getString(CREATOR_ID);

        String title = body.getString(TITLE);
        String content = body.getString(CONTENT);

        boolean hasImage = body.getBoolean(HASIMAGE);

        SubThread subThread;

        collectionName = "Subthread";
        DBName = "ARANGO_DB";


        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO check thread exists
            // TODO check creator exists

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, collectionName)) {
                arango.createCollection(arangoDB, DBName, collectionName, false);
            }

            BaseDocument myObject = new BaseDocument();

            myObject.addAttribute("ParentThreadId", parentThreadId);
            myObject.addAttribute("CreatorId", creatorId);
            myObject.addAttribute("Title", title);
            myObject.addAttribute("Content", content);
            myObject.addAttribute("Likes", INITIAL_LIKES);
            myObject.addAttribute("Dislikes", INITIAL_DISLIKES);
            myObject.addAttribute("HasImage", hasImage);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            myObject.addAttribute("DateCreated", sqlDate);

            BaseDocument res = arango.createDocument(arangoDB, DBName, collectionName, myObject);

            System.out.println(res);
            System.out.println("----------");

            String subThreadId = res.getKey();
            parentThreadId = (String) res.getAttribute("ParentThreadId");
            creatorId = (String) res.getAttribute("CreatorId");

            title = (String) res.getAttribute("Title");
            content = (String) res.getAttribute("Content");

            String date = (String) res.getAttribute("DateCreated");
            hasImage = (Boolean) res.getAttribute("HasImage");

            int likes = (int) res.getAttribute("Likes");
            int dislikes = (int) res.getAttribute("Dislikes");

            // TODO validate correct insertion

            subThread = SubThread.createNewSubThread(parentThreadId, creatorId, title, content, hasImage);
            subThread.setId(subThreadId);
            subThread.setDateCreated(date);
            subThread.setLikes(likes);
            subThread.setDislikes(dislikes);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
        return Responder.makeDataResponse(subThread.toJSON()).toString();

    }

    public static void main(String[] args) {
        CreateSubThread tc = new CreateSubThread();

        JSONObject body = new JSONObject();
        body.put("parentThreadId", "asmakElRayes7amido");
        body.put("creatorId", "sd54sdsda");
        body.put("title", "first");
        body.put("content", "first subthread ever!!!");
        body.put("hasImage", "false");


        JSONObject uriParams = new JSONObject();

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(tc.execute(request));
    }

}
