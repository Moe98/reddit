package org.sab.models.report;

public enum SubThreadReportAttributes {

    // variables in a Report object
    Report_Id("id", null),


    SUBTHREAD_Id("subthreadId", "SubthreadId"),
    DATE_CREATED("dateCreated", "DateCreated"),

    REPORTER_ID("userId", "UserId"),
    TYPE_OF_REPORT ("typeOfReport", "TypeOfReport"),


    PARENT_THREAD_ID("threadId", "ThreadId"),
    REPORT_MSG("reportMsg", "ReportMessage");


    private final String http;
    private final String db;

    SubThreadReportAttributes(String http, String db) {
        this.http = http;
        this.db = db;
    }

    public String getHTTP() {
        return http;
    }

    public String getDb() {
        return db;
    }
}
