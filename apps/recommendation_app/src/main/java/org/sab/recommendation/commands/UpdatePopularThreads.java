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

public class UpdatePopularThreads extends Command {
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
                    FOR thread IN Threads
                        SORT thread.NumOfFollowers DESC
                        LIMIT 100
                        RETURN thread""";
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, null);

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

                if (!couchbase.bucketExists(cluster, "Listings")) {
                    couchbase.createBucket(cluster, "Listings", 100);
                }

                JsonObject object = JsonObject.create().put("listOfThreads", JacksonTransformers.stringToJsonArray(response.getJSONArray("data").toString()));
                couchbase.upsertDocument(cluster, "Listings", "popThreads", object);
                response.put("msg", "Popular Threads Updated Successfully!");
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
