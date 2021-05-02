package org.sab.recommendation.commands;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.couchbase.Couchbase;
import org.sab.service.Command;

public class GetPopularThreads extends Command {
    private Couchbase couchbase;
    private Cluster cluster;

    @Override
    public String execute(JSONObject request) {
        JSONObject response = new JSONObject();
        try {
            couchbase = Couchbase.getInstance();
            cluster = couchbase.connect();

            if (!couchbase.bucketExists(cluster, "Listings") || !couchbase.documentExists(cluster, "Listings", "popThreads")) {
                String externalCommandResponseString = new UpdatePopularThreads().execute(null);
                if (new JSONObject(externalCommandResponseString).getInt("statusCode") != 200)
                    return externalCommandResponseString;
            }

            JsonObject result = couchbase.getDocument(cluster, "Listings", "popThreads");
            JsonNode data = new ObjectMapper().readTree(result.toString()).get("listOfThreads");
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
