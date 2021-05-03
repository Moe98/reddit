package org.sab.models.report;

import org.json.JSONObject;

public class SubThreadReport {
    private String subThreadId;
    private String userName;
    private TypeOfReport typeOfReport;
    private String date;
    private String reportDescription;

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

    public String getReportDescription() {
        return reportDescription;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    @Override
    public String toString() {
        return "SubThreadReport:{" +
                "subThreadId='" + getSubThreadId() + '\'' +
                ", userName='" + getUserName() + '\'' +
                ", typeOfReport='" + getTypeOfReport() + '\'' +
                ", date='" + getDate() + '\'' +
                ", reportDescription=" + getReportDescription() + '\'' +
                '}';
    }

    public JSONObject toJSON() {
        JSONObject subThreadReport = new JSONObject();
        subThreadReport.put("subThreadId", subThreadId);
        subThreadReport.put("userName", userName);
        subThreadReport.put("typeOfReport", typeOfReport);
        subThreadReport.put("date", date);
        subThreadReport.put("reportDescription", reportDescription);
        return subThreadReport;
    }
}
