package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.SubThread;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Iterator;
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

        Arango arango = null;

        SubThread subThread;

        try {
            String parentThreadId = body.getString(PARENT_THREAD_ID);
            String creatorId = uriParams.getString(CREATOR_ID);

            String title = body.getString(TITLE);
            String content = body.getString(CONTENT);

            boolean hasImage = body.getBoolean(HASIMAGE);

            arango = Arango.getInstance();
            arango.connectIfNotConnected();

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
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
        }
        return Responder.makeDataResponse(subThread.toJSON()).toString();

    }

    public static void main(String[] args) {
        CreateSubThread tc = new CreateSubThread();

        for(int i = 0; i < 100; i++) {
            JSONObject body = new JSONObject();
            if(i < 50) {
                body.put("parentThreadId", "asmakElRayes7amido");
            } else {
                body.put("parentThreadId", "GelatiAzza");
            }
            body.put("title", "Subthread" + i);
            body.put("content", "Content" + i);
            body.put("hasImage", "false");

            JSONObject uriParams = new JSONObject();
            if(i % 2 == 0) {
                uriParams.put("creatorId", "lujine");
            } else {
                uriParams.put("creatorId", "manta");
            }

            JSONObject request = new JSONObject();
            request.put("body", body);
            request.put("methodType", "POST");
            request.put("uriParams", uriParams);

            System.out.println(request);
            System.out.println("----------");
            System.out.println(tc.execute(request));
        }

        JSONObject body = new JSONObject();
        body.put("parentThreadId", "asmakElRayes7amido");
        body.put("title", "gelaty azza is better");
        body.put("content", "fish is ya3");
        body.put("hasImage", "false");

        JSONObject uriParams = new JSONObject();
        uriParams.put("creatorId", "lujine");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "POST");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(tc.execute(request));

        JSONObject body2 = new JSONObject();
        body2.put("parentThreadId", "GelatiAzza");
        body2.put("title", "gelaty azza is the best");
        body2.put("content", "I love ice cream");
        body2.put("hasImage", "false");

        JSONObject uriParams2 = new JSONObject();
        uriParams2.put("creatorId", "lujine");

        JSONObject request2 = new JSONObject();
        request2.put("body", body2);
        request2.put("methodType", "POST");
        request2.put("uriParams", uriParams2);

        System.out.println(request2);
        System.out.println("----------");

        System.out.println(tc.execute(request2));

        Arango arango = Arango.getInstance();
        ArangoCursor<BaseDocument> cursor = arango.filterCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, CREATOR_ID_DB, "lujine");
        ArrayList<String> arr = new ArrayList<>();
        arr.add(PARENT_THREAD_ID_DB);
        arr.add(CREATOR_ID_DB);
        arr.add(TITLE_DB);
        arr.add(CONTENT_DB);
        arr.add(LIKES_DB);
        arr.add(DISLIKES_DB);
        System.out.println("Lujine's threads:");
        JSONArray jsonArr = arango.parseOutput(cursor, SUBTHREAD_ID_DB, arr);
        for (Iterator<Object> it = jsonArr.iterator(); it.hasNext(); ) {
            JSONObject j = (JSONObject) it.next();
            System.out.println(j.toString());
        }

        cursor = arango.filterCollection(DB_Name, SUBTHREAD_COLLECTION_NAME, CREATOR_ID_DB, "manta");
        System.out.println("Manta's threads:");
        jsonArr = arango.parseOutput(cursor, SUBTHREAD_ID_DB, arr);
        for (Iterator<Object> it = jsonArr.iterator(); it.hasNext(); ) {
            JSONObject j = (JSONObject) it.next();
            System.out.println(j.toString());
        }


    }
}
