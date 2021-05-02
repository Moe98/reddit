package org.sab.models.report;

public class SubThreadReport extends Report{
    private String subThreadId;

    public SubThreadReport(){

    }

    public SubThreadReport(String userId, TypeOfReport typeOfReport, String data, String reportDescription, String subThreadID){
        setUserId(userId);
        setTypeOfReport(typeOfReport);
        setDate(data);
        setReportDescription(reportDescription);
        setSubThreadId(subThreadID);
    }

    public void setSubThreadId(String subThreadId) {
        this.subThreadId = subThreadId;
    }

    public String getSubThreadId() {
        return subThreadId;
    }

    @Override
    public String toString() {
        return "SubThreadReport:{" +
                "userId='" + getUserId() + '\'' +
                ", typeOfReport='" +getTypeOfReport() + '\'' +
                ", date='" + getDate() + '\'' +
                ", numOfFollowers=" + getReportDescription() + '\'' +
                ", subThreadId='" + getSubThreadId() + '\'' +
                '}';
    }
}
