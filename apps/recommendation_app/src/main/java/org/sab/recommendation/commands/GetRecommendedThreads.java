package org.sab.recommendation.commands;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.sab.couchbase.Couchbase;
import org.sab.service.Command;

public class GetRecommendedThreads extends Command {
    private Couchbase couchbase;
    private Cluster cluster;

    @Override
    public String execute(JSONObject request) {
        JsonNodeFactory nf = JsonNodeFactory.instance;
        ObjectNode response = nf.objectNode();
        try {
            couchbase = Couchbase.getInstance();
            cluster = couchbase.connect();

            JsonObject result = couchbase.getDocument(cluster, "RecommendedThreads", request.getJSONObject("body").getString("username"));
            JsonNode data = new ObjectMapper().readTree(result.toString()).get("listOfThreads");
            response.set("data", data);
            response.set("statusCode", nf.numberNode(200));
        } catch (Exception e) {
            response.set("msg", nf.textNode(e.getMessage()));
            response.set("data", nf.arrayNode());
            response.set("statusCode", nf.numberNode(500));
        } finally {
            couchbase.disconnect(cluster);
        }
        return response.toString();
    }
}
