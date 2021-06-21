package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.*;
import org.sab.models.report.SubThreadReportAttributes;
import org.sab.models.user.UserAttributes;
import org.sab.service.validation.CommandWithVerification;

public abstract class SubThreadCommand extends CommandWithVerification {

    // Subthread attributes
    // http
    protected static final String SUBTHREAD_ID = SubThreadAttributes.SUBTHREAD_ID.getHTTP();
    protected static final String PARENT_THREAD_ID = SubThreadAttributes.PARENT_THREAD_ID.getHTTP();
    protected static final String CREATOR_ID = SubThreadAttributes.CREATOR_ID.getHTTP();

    protected static final String TITLE = SubThreadAttributes.TITLE.getHTTP();
    protected static final String CONTENT = SubThreadAttributes.CONTENT.getHTTP();

    protected static final String HAS_IMAGE = SubThreadAttributes.HAS_IMAGE.getHTTP();

    // Subthread attributes
    // db
    protected static final String SUBTHREAD_ID_DB = SubThreadAttributes.SUBTHREAD_ID.getDb();
    protected static final String PARENT_THREAD_ID_DB = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    protected static final String CREATOR_ID_DB = SubThreadAttributes.CREATOR_ID.getDb();

    protected static final String TITLE_DB = SubThreadAttributes.TITLE.getDb();
    protected static final String CONTENT_DB = SubThreadAttributes.CONTENT.getDb();

    protected static final String LIKES_DB = SubThreadAttributes.LIKES.getDb();
    protected static final String DISLIKES_DB = SubThreadAttributes.DISLIKES.getDb();

    protected static final String HAS_IMAGE_DB = SubThreadAttributes.HAS_IMAGE.getDb();
    protected static final String DATE_CREATED_DB = SubThreadAttributes.DATE_CREATED.getDb();

    // Report attributes
    // http
    protected static final String REPORTED_SUBTHREAD_ID = SubThreadReportAttributes.SUBTHREAD_Id.getHTTP();
    protected static final String REPORTER_ID = SubThreadReportAttributes.REPORTER_ID.getHTTP();
    protected static final String TYPE_OF_REPORT = SubThreadReportAttributes.TYPE_OF_REPORT.getHTTP();

    protected static final String THREAD_ID = SubThreadReportAttributes.PARENT_THREAD_ID.getHTTP();
    protected static final String REPORT_MSG = SubThreadReportAttributes.REPORT_MSG.getHTTP();

    // Report atributes
    // db
    protected static final String REPORT_ID_DB = SubThreadReportAttributes.Report_Id.getDb();
    protected static final String REPORTER_ID_DB = SubThreadReportAttributes.REPORTER_ID.getDb();
    protected static final String TYPE_OF_REPORT_DB = SubThreadReportAttributes.TYPE_OF_REPORT.getDb();

    protected static final String THREAD_ID_DB = SubThreadReportAttributes.PARENT_THREAD_ID.getDb();
    protected static final String REPORT_MSG_DB = SubThreadReportAttributes.REPORT_MSG.getDb();

    // messages
    protected static final String OBJECT_NOT_FOUND = "The data you have requested does not exist.";
    protected static final String REQUESTER_NOT_AUTHOR = "You are not the author of this comment";

    // Thread attributes
    // http
    protected static final String THREAD_DESCRIPTION_DB = ThreadAttributes.DESCRIPTION.getDb();
    protected static final String THREAD_CREATOR_ID_DB = ThreadAttributes.CREATOR_ID.getDb();
    protected static final String THREAD_NUM_OF_FOLLOWERS_DB = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    protected static final String THREAD_DATE_CREATED_DB = ThreadAttributes.DATE_CREATED.getDb();

    // User attributes
    protected static final String USERNAME = UserAttributes.USERNAME.toString();

    protected static final String USER_IS_DELETED_DB = UserAttributes.IS_DELETED.getArangoDb();
    protected static final String IS_DELETED_DB = UserAttributes.IS_DELETED.getArangoDb();

    protected static final String USER_NUM_OF_FOLLOWERS_DB = UserAttributes.NUM_OF_FOLLOWERS.getArangoDb();
    protected static final String NUM_OF_FOLLOWERS_DB = UserAttributes.NUM_OF_FOLLOWERS.getArangoDb();

    protected static final String USER_ID_DB = UserAttributes.USER_ID.getArangoDb();

    // Comment attributes
    // db
    protected static final String PARENT_SUBTHREAD_ID_DB = CommentAttributes.PARENT_SUBTHREAD_ID.getDb();
    protected static final String COMMENT_ID_DB = CommentAttributes.COMMENT_ID.getDb();
    protected static final String PARENT_CONTENT_TYPE_DB = CommentAttributes.PARENT_CONTENT_TYPE.getDb();

    // Database attributes
    protected static final String DB_Name = System.getenv("ARANGO_DB");

    protected static final String THREAD_COLLECTION_NAME = CollectionNames.THREAD.get();
    protected static final String SUBTHREAD_COLLECTION_NAME = CollectionNames.SUBTHREAD.get();
    protected static final String USER_COLLECTION_NAME = CollectionNames.USER.get();
    protected static final String COMMENT_COLLECTION_NAME = CollectionNames.COMMENT.get();

    protected static final String USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME = CollectionNames.USER_BOOKMARK_SUBTHREAD.get();
    protected static final String USER_LIKE_SUBTHREAD_COLLECTION_NAME = CollectionNames.USER_LIKE_SUBTHREAD.get();
    protected static final String USER_DISLIKE_SUBTHREAD_COLLECTION_NAME = CollectionNames.USER_DISLIKE_SUBTHREAD.get();
    protected static final String SUBTHREAD_REPORTS_COLLECTION_NAME = CollectionNames.SUBTHREAD_REPORTS.get();
    protected static final String USER_MOD_THREAD_COLLECTION_NAME = CollectionNames.USER_MOD_THREAD.get();
    protected static final String USER_CREATE_SUBTHREAD_COLLECTION_NAME = CollectionNames.USER_CREATE_SUBTHREAD.get();

    // image attributes
    protected static final String BUCKETNAME = "subthread-picture-scaleabull";


    // TODO get queueName from somewhere instead of hardcoding it
    protected static final String Notification_Queue_Name = "NOTIFICATION_REQ";
    // TODO get function name from somewhere consitant
    protected static final String SEND_NOTIFICATION_FUNCTION_NAME = "SEND_NOTIFICATION";

    protected final JSONObject baseDocumentToJson(BaseDocument document) {
        SubThread subThread;

        final String subThreadId = document.getKey();
        final String parentThreadId = (String) document.getAttribute(PARENT_THREAD_ID_DB);
        final String creatorId = (String) document.getAttribute(CREATOR_ID_DB);
        final String title = (String) document.getAttribute(TITLE_DB);
        final String content = (String) document.getAttribute(CONTENT_DB);
        final String date = (String) document.getAttribute(DATE_CREATED_DB);
        final Boolean hasImage = (Boolean) document.getAttribute(HAS_IMAGE_DB);
        final int likes = Integer.parseInt(String.valueOf(document.getAttribute(LIKES_DB)));
        final int dislikes = Integer.parseInt(String.valueOf(document.getAttribute(DISLIKES_DB)));
        // TODO add tagged users in image attribute
        subThread = SubThread.createNewSubThread(parentThreadId, creatorId, title, content, hasImage);
        subThread.setId(subThreadId);
        subThread.setDateCreated(date);
        subThread.setLikes(likes);
        subThread.setDislikes(dislikes);

        return subThread.toJSON();
    }

    protected final boolean existsInCouchbase(String key) {
        return Couchbase.getInstance().documentExists(CouchbaseBuckets.RECOMMENDED_SUB_THREADS.get(), key);
    }

    protected final boolean existsInArango(String collectionName, String key) {
        return Arango.getInstance().documentExists(DB_Name, collectionName, key);
    }

    protected final void deleteDocumentFromCouchbase(String bucketName, String key) {
        Couchbase.getInstance().deleteDocumentIfExists(bucketName, key);
    }

    protected final BaseDocument getDocumentFromCouchbase(String bucketName, String key) {
        JSONObject subthread = Couchbase.getInstance().getDocumentJson(bucketName, key);

        BaseDocument myObject = new BaseDocument();

        myObject.addAttribute(PARENT_THREAD_ID_DB, subthread.get(PARENT_THREAD_ID_DB));
        myObject.addAttribute(CREATOR_ID_DB, subthread.get(CREATOR_ID_DB));
        myObject.addAttribute(TITLE_DB, subthread.get(TITLE_DB));
        myObject.addAttribute(CONTENT_DB, subthread.get(CONTENT_DB));
        myObject.addAttribute(LIKES_DB, subthread.get(LIKES_DB));
        myObject.addAttribute(DISLIKES_DB, subthread.get(DISLIKES_DB));
        myObject.addAttribute(HAS_IMAGE_DB, subthread.get(HAS_IMAGE_DB));

        return myObject;
    }

    protected final void replaceDocumentFromCouchbase(String bucketName, String key, BaseDocument updatedDoc) {
        Couchbase.getInstance().replaceDocument(bucketName, key, baseDocumentToJson(updatedDoc));
    }

    protected final void upsertDocumentInCouchbase(String bucketName, String key, BaseDocument updatedDoc) {
        Couchbase.getInstance().upsertDocument(bucketName, key, baseDocumentToJson(updatedDoc));
    }
}
