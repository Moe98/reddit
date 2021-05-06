package org.sab.models.report;

import org.json.JSONObject;

public class SubThreadReport {
    private String subThreadId;
    private String parentThreadId;
    private String userName;
    private TypeOfReport typeOfReport;
    private String date;
    private String reportMessage;

    public String getSubThreadId() {
        return subThreadId;
    }

    public void setSubThreadId(String subThreadId) {
        this.subThreadId = subThreadId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public TypeOfReport getTypeOfReport() {
        return typeOfReport;
    }

    public void setTypeOfReport(TypeOfReport typeOfReport) {
        this.typeOfReport = typeOfReport;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    @Override
    public String toString() {
        return "SubThreadReport:{" +
                "subThreadId='" + getSubThreadId() + '\'' +
                ", userName='" + getUserName() + '\'' +
                ", typeOfReport='" + getTypeOfReport() + '\'' +
                ", date='" + getDate() + '\'' +
                ", reportDescription='" + getReportMessage() + '\'' +
                ", parentThreadId='" + getParentThreadId() + '\'' +
                '}';
    }

    public JSONObject toJSON() {
        JSONObject subThreadReport = new JSONObject();
        subThreadReport.put(SunThreadReportAttributes.SUBTHREAD_Id.getHTTP(), subThreadId);
        subThreadReport.put(SunThreadReportAttributes.REPORTER_ID.getHTTP(), userName);
        subThreadReport.put(SunThreadReportAttributes.PARENT_THREAD_ID.getHTTP(), parentThreadId);
        subThreadReport.put(SunThreadReportAttributes.TYPE_OF_REPORT.getHTTP(), typeOfReport);
        subThreadReport.put(SunThreadReportAttributes.DATE_CREATED.getHTTP(), date);
        subThreadReport.put(SunThreadReportAttributes.REPORT_MSG.getHTTP(), reportMessage);
        return subThreadReport;
    }

    public String getParentThreadId() {
        return parentThreadId;
    }

    public void setParentThreadId(String parentThreadId) {
        this.parentThreadId = parentThreadId;
    }
}
