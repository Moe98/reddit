package org.sab.search.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Command;

import java.util.Collections;
import java.util.Map;

public class SearchSubThread extends Command {
    private Arango arango;
    private ArangoDB arangoDB;

    @Override
    public String execute(JSONObject request) {
        JSONObject response = new JSONObject();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // Create an ArangoSearchView on collection SubThreads using english text analyzer on Title & Content attributes.
            arango.createViewIfNotExists(arangoDB, System.getenv("ARANGO_DB"), "SubThreadsView", "SubThreads", new String[]{"Title", "Content"});

            String query = """
                    FOR result IN SubThreadsView
                         SEARCH ANALYZER(result.Title IN TOKENS(@words, "text_en") OR result.Content IN TOKENS(@words, "text_en"), "text_en")
                         RETURN result""";
            Map<String, Object> bindVars = Collections.singletonMap("words", request.getJSONObject("body").getString("searchText"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, bindVars);

            JSONArray data = new JSONArray();
            if (cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    JSONObject subThread = new JSONObject();
                    subThread.put("_key", document.getKey());
                    subThread.put("ParentThread", document.getProperties().get("ParentThread"));
                    subThread.put("Title", document.getProperties().get("Title"));
                    subThread.put("Creator", document.getProperties().get("Creator"));
                    subThread.put("Likes", document.getProperties().get("Likes"));
                    subThread.put("Dislikes", document.getProperties().get("Dislikes"));
                    subThread.put("Content", document.getProperties().get("Content"));
                    subThread.put("HasImage", document.getProperties().get("HasImage"));
                    subThread.put("Time", document.getProperties().get("Time"));
                    data.put(subThread);
                });
                response.put("data", data);
            } else {
                response.put("msg", "No Result");
                response.put("data", new JSONArray());
            }
            response.put("statusCode", 200);
        } catch (Exception e) {
            response.put("msg", e.getMessage());
            response.put("data", new JSONArray());
            response.put("statusCode", 500);
        } finally {
            arango.disconnect(arangoDB);
        }
        return response.toString();
    }
}
