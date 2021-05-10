package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.SubThread;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class CreateSubThread extends SubThreadCommand {
    final long INITIAL_LIKES = 0;
    final long INITIAL_DISLIKES = 0;

    @Override
    protected Schema getSchema() {
        Attribute parentThreadId = new Attribute(PARENT_THREAD_ID, DataType.STRING, true);

        Attribute title = new Attribute(TITLE, DataType.STRING, true);
        Attribute content = new Attribute(CONTENT, DataType.STRING, true);

        Attribute hasImage = new Attribute(HASIMAGE, DataType.BOOLEAN, true);

        return new Schema(List.of(parentThreadId, title, content, hasImage));
    }


    @Override
    protected String execute() {
        String parentThreadId = body.getString(PARENT_THREAD_ID);
        String creatorId = uriParams.getString(CREATOR_ID);

        String title = body.getString(TITLE);
        String content = body.getString(CONTENT);

        boolean hasImage = body.getBoolean(HASIMAGE);

        SubThread subThread;

        try {
            Arango arango = Arango.getInstance();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(DB_Name, SUBTHREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, false);
            }
            // TODO check thread exists
            String msg;
            if(!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, parentThreadId)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            BaseDocument myObject = new BaseDocument();

            myObject.addAttribute(PARENT_THREAD_ID_DB, parentThreadId);
            myObject.addAttribute(CREATOR_ID_DB, creatorId);
            myObject.addAttribute(TITLE_DB, title);
            myObject.addAttribute(CONTENT_DB, content);
            myObject.addAttribute(LIKES_DB, INITIAL_LIKES);
            myObject.addAttribute(DISLIKES_DB, INITIAL_DISLIKES);
            // TODO handle adding the image to the DB
            myObject.addAttribute(HASIMAGE_DB, hasImage);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            myObject.addAttribute(DATE_CREATED_DB, sqlDate);

            BaseDocument res = arango.createDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, myObject);

            String subThreadId = res.getKey();
            parentThreadId = (String) res.getAttribute(PARENT_THREAD_ID_DB);
            creatorId = (String) res.getAttribute(CONTENT_DB);

            title = (String) res.getAttribute(TITLE_DB);
            content = (String) res.getAttribute(CONTENT_DB);

            String date = (String) res.getAttribute(DATE_CREATED_DB);
            hasImage = (Boolean) res.getAttribute(HASIMAGE_DB);

            int likes = Integer.parseInt(String.valueOf(res.getAttribute(LIKES_DB)));
            int dislikes = Integer.parseInt(String.valueOf(res.getAttribute(DISLIKES_DB)));

            // TODO validate correct insertion

            subThread = SubThread.createNewSubThread(parentThreadId, creatorId, title, content, hasImage);
            subThread.setId(subThreadId);
            subThread.setDateCreated(date);
            subThread.setLikes(likes);
            subThread.setDislikes(dislikes);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
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
        uriParams.put("creatorId", "32930");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(tc.execute(request));
    }
}
