package org.sab.subthread.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Schema;
import java.util.Collections;
import java.util.Map;

public class ModeratorSeeReorts extends SubThreadCommand {
    @Override
    protected Schema getSchema() {
        return null;
    }


    private Arango arango;
    private ArangoDB arangoDB;
    private String CollectionName;
    private String DBName;


    @Override
    protected String execute() {
        String threadId = uriParams.getString(THREAD_ID);

        CollectionName = "SubthreadReports";

        DBName = "ARANGO_DB";

        JSONObject response = new JSONObject();

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            String query = """
                    FOR subthread IN SubThreadReports
                        FILTER subthread.ReportedContent == @threadId 
                        
                        RETURN subthread""";

            Map<String, Object> bindVars = Collections.singletonMap("threadId", threadId);
            // TODO: System.getenv("ARANGO_DB") instead of writing the DB
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, bindVars);

            JSONArray data = new JSONArray();
            if (cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    JSONObject subthread = new JSONObject();
                    subthread.put("User", document.getProperties().get("User"));
                    subthread.put("TypeOfReport", document.getProperties().get("TypeOfReport"));
                    subthread.put("ReportedContent", document.getProperties().get("ReportedContent"));
                    subthread.put("Date", document.getProperties().get("Date"));
                    subthread.put("Report", document.getProperties().get("Report"));
                    subthread.put("SubThreadID", document.getProperties().get("SubThreadID"));
                    data.put(subthread);
                });
                response.put("data", data);
            } else {
                response.put("msg", "No Reports Present");
                response.put("data", new JSONArray());
            }
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
        return Responder.makeDataResponse(response).toString();
    }
}
