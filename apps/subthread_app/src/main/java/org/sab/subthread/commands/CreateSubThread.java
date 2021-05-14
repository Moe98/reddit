package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.SubThread;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;


import java.util.List;

public class CreateSubThread extends SubThreadCommand {
    final long INITIAL_LIKES = 0;
    final long INITIAL_DISLIKES = 0;

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

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
            String creatorId = authenticationParams.getString(SubThreadCommand.USERNAME);
            String parentThreadId = body.getString(PARENT_THREAD_ID);
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
            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, parentThreadId)) {
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
            myObject.addAttribute(HASIMAGE_DB, hasImage);
            
            if(hasImage) {
                // TODO handle adding the image to the DB
                
            }

            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            myObject.addAttribute(DATE_CREATED_DB, sqlDate);

            BaseDocument res = arango.createDocument(DB_Name, SUBTHREAD_COLLECTION_NAME, myObject);

            String subThreadId = res.getKey();
            parentThreadId = (String) res.getAttribute(PARENT_THREAD_ID_DB);
            creatorId = (String) res.getAttribute(CREATOR_ID_DB);

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
}
