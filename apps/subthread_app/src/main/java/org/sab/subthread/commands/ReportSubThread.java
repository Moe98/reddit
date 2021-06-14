package org.sab.subthread.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.NotificationMessages;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

import static org.sab.innerAppComm.Comm.notifyApp;

public class ReportSubThread extends SubThreadCommand {

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected Schema getSchema() {
        Attribute typeOfReport = new Attribute(TYPE_OF_REPORT, DataType.STRING, true);
        Attribute subthreadId = new Attribute(REPORTED_SUBTHREAD_ID, DataType.STRING, true);
        Attribute threadId = new Attribute(THREAD_ID, DataType.STRING, true);
        Attribute reportMsg = new Attribute(REPORT_MSG, DataType.STRING, true);
        return new Schema(List.of(typeOfReport, subthreadId, threadId, reportMsg));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    public String execute() {

        Arango arango = null;

        JSONObject response = new JSONObject();
        String msg = "";

        try {
            String userId = authenticationParams.getString(SubThreadCommand.USERNAME);

            String typeOfReport = body.getString(TYPE_OF_REPORT);
            String subthreadId = body.getString(REPORTED_SUBTHREAD_ID);
            String threadId = body.getString(THREAD_ID);
            String reportMsg = body.getString(REPORT_MSG);

            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            arango.createCollectionIfNotExists(DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME, false);

            // check if thread exists
            if (!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadId)) {
                msg = "Thread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // check if subthread exists
            if (!arango.documentExists(DB_Name, SUBTHREAD_COLLECTION_NAME, subthreadId)) {
                msg = "Subthread does not exist";
                return Responder.makeErrorResponse(msg, 400).toString();
            }

            // TODO check if subthread belongs to thread!

            BaseDocument myObject = new BaseDocument();
            myObject.addAttribute(REPORTER_ID_DB, userId);
            myObject.addAttribute(TYPE_OF_REPORT_DB, typeOfReport);
            myObject.addAttribute(THREAD_ID_DB, threadId);
            java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
            myObject.addAttribute(DATE_CREATED_DB, sqlDate);
            myObject.addAttribute(REPORT_MSG_DB, reportMsg);
            myObject.addAttribute(REPORTED_SUBTHREAD_ID, subthreadId);

            BaseDocument res = arango.createDocument(DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME, myObject);
            msg = "Created Subthread Report";

            //notify the user who is reporting
            notifyApp(Notification_Queue_Name, NotificationMessages.SUBTHREAD_REPORT_MSG.getMSG(), subthreadId, userId, SEND_NOTIFICATION_FUNCTION_NAME);

        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            if (arango != null) {
                arango.disconnect();
            }
            response.put("msg", msg);
        }
        return Responder.makeDataResponse(response).toString();
    }
}
