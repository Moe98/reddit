package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Schema;
import java.util.Collections;
import java.util.Map;

public class ModeratorSeeReports extends SubThreadCommand {
    @Override
    protected Schema getSchema() {
        return null;
    }

    @Override
    protected String execute() {
        String threadId = uriParams.getString(THREAD_ID);

        JSONObject response = new JSONObject();

        try {
            Arango arango = Arango.getInstance();

            // TODO can we bind the collection name in the query to SUBTHREAD_REPORTS_COLLECTION_NAME
            //  Same for the ThreadId attribute
            String query = """
                    FOR subthread IN SubThreadReports
                        FILTER subthread.ThreadId == @threadId 
                        
                        RETURN subthread""";

            Map<String, Object> bindVars = Collections.singletonMap("threadId", threadId);
            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            ArangoCursor<BaseDocument> cursor = arango.query(System.getenv("ARANGO_DB"), query, bindVars);

            JSONArray data = new JSONArray();
            if (cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    JSONObject subthread = new JSONObject();
                    subthread.put(REPORTER_ID_DB, document.getProperties().get(REPORTER_ID_DB));
                    subthread.put(TYPE_OF_REPORT_DB, document.getProperties().get(TYPE_OF_REPORT_DB));
//                    subthread.put("ReportedContent", document.getProperties().get("ReportedContent")); // thread id
                    subthread.put(THREAD_ID_DB, document.getProperties().get(THREAD_ID_DB));
                    subthread.put(DATE_CREATED_DB, document.getProperties().get(DATE_CREATED_DB));
//                    subthread.put("Report", document.getProperties().get("Report")); // message
                    subthread.put(REPORT_MSG_DB, document.getProperties().get(REPORT_MSG_DB));
                    subthread.put(REPORTED_SUBTHREAD_ID_DB, document.getProperties().get(REPORTED_SUBTHREAD_ID_DB));
                    data.put(subthread);
                });
                response.put("data", data);
            } else {
                response.put("msg", "No Reports Present");
                response.put("data", new JSONArray());
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }
        return Responder.makeDataResponse(response).toString();
    }
}
