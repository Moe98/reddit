package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;

public class ModeratorSeeReports extends SubThreadCommand {

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }

    @Override
    protected HTTPMethod getMethodType() {
        // TODO get?
        return HTTPMethod.GET;
    }

    @Override
    protected String execute() {
        Arango arango = null;

        JSONArray response = new JSONArray();

        try {
            // TODO why is the thread id not the user id in the URI?
            String threadId = uriParams.getString(THREAD_ID);

            arango = Arango.getInstance();

            if (!arango.collectionExists(DB_Name, THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, THREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, SUBTHREAD_REPORTS_COLLECTION_NAME, false);
            }

            ArangoCursor<BaseDocument> cursor = arango.filterCollection(SubThreadCommand.DB_Name, SubThreadCommand.SUBTHREAD_REPORTS_COLLECTION_NAME, SubThreadCommand.THREAD_ID_DB, threadId);
            ArrayList<String> reportAtt = new ArrayList<>();
            reportAtt.add(SubThreadCommand.REPORTER_ID_DB);
            reportAtt.add(SubThreadCommand.TYPE_OF_REPORT_DB);
            reportAtt.add(SubThreadCommand.THREAD_ID_DB);
            reportAtt.add(SubThreadCommand.DATE_CREATED_DB);
            reportAtt.add(SubThreadCommand.REPORT_MSG_DB);
            reportAtt.add(SubThreadCommand.SUBTHREAD_ID_DB);
            response = arango.parseOutput(cursor, SubThreadCommand.REPORT_ID_DB, reportAtt);
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }
        return Responder.makeDataResponse(response).toString();
    }
}
