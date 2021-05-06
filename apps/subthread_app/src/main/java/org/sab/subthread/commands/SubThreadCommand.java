package org.sab.subthread.commands;

import org.sab.models.SubThreadAttributes;
import org.sab.models.report.SunThreadReportAttributes;
import org.sab.service.validation.CommandWithVerification;

public abstract class SubThreadCommand extends CommandWithVerification {
    
    // Subthread attributes
    // http
    protected static final String SUBTHREAD_ID = SubThreadAttributes.SUBTHREAD_ID.getHTTP();
    protected static final String PARENT_THREAD_ID = SubThreadAttributes.PARENT_THREAD_ID.getHTTP();
    protected static final String CREATOR_ID = SubThreadAttributes.CREATOR_ID.getHTTP();

    protected static final String TITLE = SubThreadAttributes.TITLE.getHTTP();
    protected static final String CONTENT = SubThreadAttributes.CONTENT.getHTTP();

    protected static final String LIKES = SubThreadAttributes.LIKES.getHTTP();
    protected static final String DISLIKES = SubThreadAttributes.DISLIKES.getHTTP();

    protected static final String HASIMAGE = SubThreadAttributes.HAS_IMAGE.getHTTP();

    // Subthread attributes
    // db
    protected static final String PARENT_THREAD_ID_DB = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    protected static final String CREATOR_ID_DB = SubThreadAttributes.CREATOR_ID.getDb();

    protected static final String TITLE_DB = SubThreadAttributes.TITLE.getDb();
    protected static final String CONTENT_DB = SubThreadAttributes.CONTENT.getDb();

    protected static final String LIKES_DB = SubThreadAttributes.LIKES.getDb();
    protected static final String DISLIKES_DB = SubThreadAttributes.DISLIKES.getDb();

    protected static final String HASIMAGE_DB = SubThreadAttributes.HAS_IMAGE.getDb();
    protected static final String DATE_CREATED_DB = SubThreadAttributes.DATE_CREATED.getDb();

    // Report atributes
    // http
    protected static final String REPORTED_SUBTHREAD_ID = SunThreadReportAttributes.SUBTHREAD_Id.getHTTP();
    protected static final String REPORTER_ID = SunThreadReportAttributes.REPORTER_ID.getHTTP();
    protected static final String TYPE_OF_REPORT = SunThreadReportAttributes.TYPE_OF_REPORT.getHTTP();

    protected static final String THREAD_ID = SunThreadReportAttributes.PARENT_THREAD_ID.getHTTP();
    protected static final String REPORT_MSG = SunThreadReportAttributes.REPORT_MSG.getHTTP();
    
    // Report atributes
    // db
    protected static final String REPORTED_SUBTHREAD_ID_DB = SunThreadReportAttributes.SUBTHREAD_Id.getDb();
    protected static final String REPORTER_ID_DB = SunThreadReportAttributes.REPORTER_ID.getDb();
    protected static final String TYPE_OF_REPORT_DB = SunThreadReportAttributes.TYPE_OF_REPORT.getDb();

    protected static final String THREAD_ID_DB = SunThreadReportAttributes.PARENT_THREAD_ID.getDb();
    protected static final String REPORT_MSG_DB = SunThreadReportAttributes.REPORT_MSG.getDb();

    // Database attributes
    protected static final String DB_Name = "ARANGO_DB";

    protected static final String SUBTHREAD_COLLECTION_NAME = "Subthread";
    protected static final String USER_COLLECTION_NAME = "User";

    protected static final String USER_BOOKMARK_SUBTHREAD_COLLECTION_NAME = "UserBookmarkSubthread";
    protected static final String USER_LIKE_SUBTHREAD_COLLECTION_NAME = "UserLikeSubthread";
    protected static final String USER_DISLIKE_SUBTHREAD_COLLECTION_NAME = "UserDislikeSubthread";
    protected static final String SUBTHREAD_REPORTS_COLLECTION_NAME = "SubthreadReports";

}
