package org.sab.recommendation.commands;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.couchbase.Couchbase;
import org.sab.service.Command;

public class GetRecommendedUsers extends Command {
    private Couchbase couchbase;
    private Cluster cluster;

    @Override
    public String execute(JSONObject request) {
        JSONObject response = new JSONObject();
        try {
            couchbase = Couchbase.getInstance();
            cluster = couchbase.connect();

            if (!couchbase.bucketExists(cluster, "RecommendedUsers") || !couchbase.documentExists(cluster, "RecommendedUsers", request.getJSONObject("body").getString("username"))) {
//                String externalCommandResponseString = new UpdateRecommendedUsers().execute(request);
//                if(new JSONObject(externalCommandResponseString).getInt("statusCode") != 200)
//                    return externalCommandResponseString;
            }

            JsonObject result = couchbase.getDocument(cluster, "RecommendedUsers", request.getJSONObject("body").getString("username"));
            JsonNode data = new ObjectMapper().readTree(result.toString()).get("listOfUsers");
            response.put("data", new JSONArray(data.toString()));
            response.put("statusCode", 200);
        } catch (Exception e) {
            response.put("msg", e.getMessage());
            response.put("data", new JSONArray());
            response.put("statusCode", 500);
        } finally {
            couchbase.disconnect(cluster);
        }
        return response.toString();
    }
}
