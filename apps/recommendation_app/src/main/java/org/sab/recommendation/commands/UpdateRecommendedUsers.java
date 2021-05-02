package org.sab.recommendation.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.json.JacksonTransformers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.service.Command;

import java.util.Collections;
import java.util.Map;

public class UpdateRecommendedUsers extends Command {
    private Arango arango;
    private ArangoDB arangoDB;
    private Couchbase couchbase;
    private Cluster cluster;

    @Override
    public String execute(JSONObject request) {
        JSONObject response = new JSONObject();

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();
            String query = """
                    FOR user IN 2..2 OUTBOUND @username UserFollowUser
                                         Filter user._id != @username
                                         COLLECT friend = user._key
                                         WITH COUNT INTO mutual_number
                                         SORT mutual_number DESC
                                         LIMIT 25
                                         RETURN {username:friend}""";
            Map<String, Object> bindVars = Collections.singletonMap("username", "Users/" + request.getJSONObject("body").getString("username"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, bindVars);

            JSONArray data = new JSONArray();
            if (cursor.hasNext()) {
                cursor.forEachRemaining(document -> data.put(document.getProperties().get("username")));
                response.put("data", data);
            } else {
                response.put("msg", "No Result");
                response.put("data", new JSONArray());
            }
        } catch (Exception e) {
            response.put("msg", e.getMessage());
            response.put("data", new JSONArray());
            response.put("statusCode", 500);
        } finally {
            arango.disconnect(arangoDB);
        }

        if (response.getJSONArray("data").length() != 0) {
            try {
                couchbase = Couchbase.getInstance();
                cluster = couchbase.connect();

                if (!couchbase.bucketExists(cluster, "RecommendedUsers")) {
                    couchbase.createBucket(cluster, "RecommendedUsers", 100);
                }

                JsonObject object = JsonObject.create().put("listOfUsernames", JacksonTransformers.stringToJsonArray(response.get("data").toString()));
                couchbase.upsertDocument(cluster, "RecommendedUsers", request.getJSONObject("body").getString("username"), object);
                response.put("msg", "Recommended Users Updated Successfully!");
                response.put("statusCode", 200);
            } catch (Exception e) {
                response.put("msg", e.getMessage());
                response.put("data", new JSONArray());
                response.put("statusCode", 500);
            } finally {
                couchbase.disconnect(cluster);
            }
        }
        return response.toString();
    }
}
