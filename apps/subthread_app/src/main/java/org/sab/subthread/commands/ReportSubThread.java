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

    @Override
    public String execute() {
        String userId = uriParams.getString(REPORTER_ID);

        String typeOfReport = body.getString(TYPE_OF_REPORT);
        String subthreadId = body.getString(REPORTED_SUBTHREAD_ID);
        String threadId = body.getString(THREAD_ID);
        String reportMsg = body.getString(REPORT_MSG);
        long millis = System.currentTimeMillis();
        java.sql.Date dateCreated = new java.sql.Date(millis);

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            Arango arango = Arango.getInstance();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            if (!arango.collectionExists(DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME, false);
            }

            // check if thread exists
            if(!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadId)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // check if subthread exists
            if(!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // TODO check if subthread belongs to thread!

            BaseDocument myObject = new BaseDocument();
            myObject.addAttribute(REPORTER_ID_DB, userId);
            myObject.addAttribute(TYPE_OF_REPORT_DB, typeOfReport);
            myObject.addAttribute(THREAD_ID_DB, threadId);
            myObject.addAttribute(DATE_CREATED_DB, dateCreated);
            myObject.addAttribute(REPORT_MSG, reportMsg);
            myObject.addAttribute(SUBTHREAD_ID, subthreadId);

            BaseDocument res = arango.createDocument(DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME, myObject);
            msg = "Created Subthread Report";

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }

    public static void main(String[] args) {
        ReportSubThread tc = new ReportSubThread();

        JSONObject body = new JSONObject();
        body.put(TYPE_OF_REPORT, "SubthreadReport");
        body.put(REPORTED_SUBTHREAD_ID, "126209");
        body.put(THREAD_ID, "GelatiAzza");
        body.put(REPORT_MSG, "This highly offends me!!");

        JSONObject uriParams = new JSONObject();
        uriParams.put(REPORTER_ID, "asdafsda");


        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("----------");

        System.out.println(tc.execute(request));
    }
}
