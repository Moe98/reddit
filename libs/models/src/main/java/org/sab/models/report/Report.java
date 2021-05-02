package org.sab.models.report;

import java.util.Date;

public abstract class Report {
    private String userId;
    private TypeOfReport typeOfReport;
    private String date;
    private String reportDescription;

    public String getUserId() {
        return userId;
    }

    public TypeOfReport getTypeOfReport() {
        return typeOfReport;
    }

    public String getDate() {
        return date;
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTypeOfReport(TypeOfReport typeOfReport) {
        this.typeOfReport = typeOfReport;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }
}
