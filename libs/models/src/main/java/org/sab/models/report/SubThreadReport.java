package org.sab.models.report;

public class SubThreadReport extends Report {
    private String subThreadId;

    public SubThreadReport(String userName, TypeOfReport typeOfReport, String data, String reportDescription, String subThreadID) {
        setUserName(userName);
        setTypeOfReport(typeOfReport);
        setDate(data);
        setReportDescription(reportDescription);
        setSubThreadId(subThreadID);
    }

    public String getSubThreadId() {
        return subThreadId;
    }

    public void setSubThreadId(String subThreadId) {
        this.subThreadId = subThreadId;
    }

    @Override
    public String toString() {
        return "SubThreadReport:{" +
                "userId='" + getUserName() + '\'' +
                ", typeOfReport='" + getTypeOfReport() + '\'' +
                ", date='" + getDate() + '\'' +
                ", numOfFollowers=" + getReportDescription() + '\'' +
                ", subThreadId='" + getSubThreadId() + '\'' +
                '}';
    }
}
