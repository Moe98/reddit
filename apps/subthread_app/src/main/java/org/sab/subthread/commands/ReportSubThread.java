package org.sab.subthread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.Date;
import java.util.List;

public class ReportSubThread extends SubThreadCommand{
    @Override
    protected Schema getSchema() {
        Attribute typeOfReport = new Attribute(TYPE_OF_REPORT, DataType.STRING, true);
        Attribute subthreadId = new Attribute(SUBTHREAD_ID, DataType.STRING, true);
        Attribute threadId = new Attribute(THREAD_ID, DataType.STRING, true);
        Attribute reportMsg = new Attribute(REPORT_MSG, DataType.STRING, true);
        return new Schema(List.of(typeOfReport,subthreadId,threadId,reportMsg));
    }

    private Arango arango;
    private ArangoDB arangoDB;
    private String CollectionName;
    private String DBName;

    @Override
    public String execute() {
        String typeOfReport = body.getString(TYPE_OF_REPORT);
        String subthreadId = body.getString(SUBTHREAD_ID);
        String threadId = body.getString(THREAD_ID);
        String reportMsg = body.getString(REPORT_MSG);
        String userId = uriParams.getString(USER_ID);
        long millis = System.currentTimeMillis();
        java.sql.Date dateCreated = new java.sql.Date(millis);

        CollectionName = "SubthreadReports";

        DBName = "ARANGO_DB";

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DBName, CollectionName)) {
                arango.createCollection(arangoDB, DBName, CollectionName, false);
            }

            BaseDocument myObject = new BaseDocument();
            myObject.addAttribute("User", userId);
            myObject.addAttribute("TypeOfReport", typeOfReport);
            myObject.addAttribute("ReportedContent", threadId);
            myObject.addAttribute("Date", dateCreated);
            myObject.addAttribute("Report", reportMsg);
            myObject.addAttribute("SubThreadID", subthreadId);

            BaseDocument res = arango.createDocument(arangoDB, DBName, CollectionName, myObject);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
        return Responder.makeDataResponse(new JSONObject()).toString();
    }
}
