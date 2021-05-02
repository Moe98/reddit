package org.sab.recommendation.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JacksonTransformers;
import com.couchbase.client.java.json.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.service.Command;

public class UpdatePopularSubThreads extends Command {
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
                    FOR subThread IN SubThreads
                        SORT subThread.Time DESC
                        LIMIT 200
                        SORT SUM([subThread.Likes, subThread.Dislikes]) DESC
                        LIMIT 100
                        RETURN subThread""";
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, null);

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

                JsonObject object = JsonObject.create().put("listOfSubThreads", JacksonTransformers.stringToJsonArray(response.getJSONArray("data").toString()));
                couchbase.upsertDocument(cluster, "Listings", "popSubThreads", object);
                response.put("msg", "Popular SubThreads Updated Successfully!");
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
