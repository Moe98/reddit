package org.sab.search.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.models.Thread;
import org.sab.service.Command;

import java.util.Collections;
import java.util.Map;

public class SearchThread extends Command {
    private Arango arango;
    private ArangoDB arangoDB;

    @Override
    public String execute(JSONObject request) {
        JSONObject response = new JSONObject();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            if (!arangoDB.db(System.getenv("ARANGO_DB")).view("ThreadsView").exists()) {
                arango.createView(arangoDB, System.getenv("ARANGO_DB"), "ThreadsView", "Threads", new String[]{"_key"});
            }

            String query = """
                    FOR result IN ThreadsView
                         SEARCH ANALYZER(STARTS_WITH(result._key, LOWER(LTRIM(@words))), "text_en")
                         RETURN result""";
            Map<String, Object> bindVars = Collections.singletonMap("words", request.getJSONObject("body").getString("searchText"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, bindVars);

            JSONArray data = new JSONArray();
            if (cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    JSONObject thread = new JSONObject();
                    thread.put("_key", document.getKey());
                    thread.put("Description", document.getProperties().get("Description"));
                    thread.put("Creator", document.getProperties().get("Creator"));
                    thread.put("NumOfFollowers", document.getProperties().get("NumOfFollowers"));
                    thread.put("DateCreated", document.getProperties().get("DateCreated"));
                    data.put(thread);
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
