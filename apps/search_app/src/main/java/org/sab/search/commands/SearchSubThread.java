package org.sab.search.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        JsonNodeFactory nf = JsonNodeFactory.instance;
        ObjectNode response = nf.objectNode();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            if(!arangoDB.db(System.getenv("ARANGO_DB")).view("SubThreadsView").exists()){
                arango.createView(arangoDB, System.getenv("ARANGO_DB"), "SubThreadsView", "SubThreads", new String[] {"Title", "Content"});
            }

            String query = """
                    FOR result IN SubThreadsView
                         SEARCH ANALYZER(result.Title IN TOKENS(@words, "text_en") OR result.Content IN TOKENS(@words, "text_en"), "text_en")
                         RETURN result""";
            Map<String, Object> bindVars = Collections.singletonMap("words", request.getJSONObject("body").getString("searchText"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, bindVars);

            ArrayNode data = nf.arrayNode();
            if(cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    ObjectNode subThread = nf.objectNode();
                    subThread.put("_key", document.getKey());
                    subThread.put("ParentThread", (String) document.getProperties().get("ParentThread"));
                    subThread.put("Title", (String) document.getProperties().get("Title"));
                    subThread.put("Creator", (String) document.getProperties().get("Creator"));
                    subThread.put("Likes", (Integer) document.getProperties().get("Likes"));
                    subThread.put("Dislikes", (Integer) document.getProperties().get("Dislikes"));
                    subThread.put("Content", (String) document.getProperties().get("Content"));
                    subThread.put("HasImage", (Boolean) document.getProperties().get("HasImage"));
                    subThread.put("Time", (String) document.getProperties().get("Time"));
                    data.add(subThread);
                });
                response.set("data", data);
            }
            else{
                response.set("msg", nf.textNode("No Result"));
                response.set("data", nf.arrayNode());
            }
            response.set("statusCode", nf.numberNode(200));
        } catch (Exception e){
            response.set("msg", nf.textNode(e.getMessage()));
            response.set("data", nf.arrayNode());
            response.set("statusCode", nf.numberNode(500));
        }
        finally {
            arango.disconnect(arangoDB);
        }
        return response.toString();
    }
}
