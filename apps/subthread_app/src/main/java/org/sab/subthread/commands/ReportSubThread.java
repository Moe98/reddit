package org.sab.subthread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class ReportSubThread extends SubThreadCommand{
    @Override
    protected Schema getSchema() {
        Attribute typeOfReport = new Attribute(TYPE_OF_REPORT, DataType.STRING, true);
        Attribute subthreadId = new Attribute(REPORTED_SUBTHREAD_ID, DataType.STRING, true);
        Attribute threadId = new Attribute(THREAD_ID, DataType.STRING, true);
        Attribute reportMsg = new Attribute(REPORT_MSG, DataType.STRING, true);
        return new Schema(List.of(typeOfReport,subthreadId,threadId,reportMsg));
    }

    private Arango arango;
    private ArangoDB arangoDB;

    @Override
    public String execute() {
        String userId = uriParams.getString(REPORTER_ID);

        String typeOfReport = body.getString(TYPE_OF_REPORT);
        String subthreadId = body.getString(REPORTED_SUBTHREAD_ID);
        String threadId = body.getString(THREAD_ID);
        String reportMsg = body.getString(REPORT_MSG);
        long millis = System.currentTimeMillis();
        java.sql.Date dateCreated = new java.sql.Date(millis);


        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(arangoDB, DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME, false);
            }

            BaseDocument myObject = new BaseDocument();
            myObject.addAttribute(REPORTER_ID_DB, userId);
            myObject.addAttribute(TYPE_OF_REPORT_DB, typeOfReport);
            myObject.addAttribute(THREAD_ID_DB, threadId);
            myObject.addAttribute(DATE_CREATED_DB, dateCreated);
            myObject.addAttribute(REPORT_MSG, reportMsg);
            myObject.addAttribute(SUBTHREAD_ID, subthreadId);

            BaseDocument res = arango.createDocument(arangoDB, DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME, myObject);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
        return Responder.makeDataResponse(new JSONObject()).toString();
    }
}
