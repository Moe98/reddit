package org.sab.search.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        JsonNodeFactory nf = JsonNodeFactory.instance;
        ObjectNode response = nf.objectNode();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            if(!arangoDB.db(System.getenv("ARANGO_DB")).view("ThreadsView").exists()){
                arango.createView(arangoDB, System.getenv("ARANGO_DB"), "ThreadsView", "Threads", new String[] {"Description"});
            }

            String query = """
                    FOR result IN ThreadsView
                        SEARCH PHRASE(result.Description, @words, "text_en")
                        RETURN result""";
            Map<String, Object> bindVars = Collections.singletonMap("words", request.getJSONObject("body").getString("searchText"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, bindVars);

            Thread thread = new Thread();
            ArrayNode data = nf.arrayNode();
            if(cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    thread.setName(document.getKey());
                    thread.setDescription((String) document.getProperties().get("Description"));
                    thread.setCreator((String) document.getProperties().get("Creator"));
                    thread.setNumOfFollowers((Integer) document.getProperties().get("NumOfFollowers"));
                    thread.setDateCreated((String) document.getProperties().get("DateCreated"));
                    data.addPOJO(thread);
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
