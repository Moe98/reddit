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

import java.util.Collections;
import java.util.Map;

public class UpdateRecommendedThreads extends Command {
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

            if (!arangoDB.db(System.getenv("ARANGO_DB")).view("ThreadsView").exists()) {
                arango.createView(arangoDB, System.getenv("ARANGO_DB"), "ThreadsView", "Threads", new String[]{"_key"});
            }

            Map<String, Object> bindVars = Collections.singletonMap("username", "Users/" + request.getJSONObject("body").getString("username"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), getQuery(), bindVars);

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

                if (!couchbase.bucketExists(cluster, "RecommendedThreads")) {
                    couchbase.createBucket(cluster, "RecommendedThreads", 100);
                }

                JsonObject object = JsonObject.create().put("listOfThreads", JacksonTransformers.stringToJsonArray(response.getJSONArray("data").toString()));
                couchbase.upsertDocument(cluster, "RecommendedThreads", request.getJSONObject("body").getString("username"), object);
                response.put("msg", "Recommended Threads Updated Successfully!");
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

    public static String getQuery() {
        return """
                LET followed = (
                    FOR thread, edge IN 1..1 OUTBOUND @username UserFollowThread
                        SORT edge.date DESC
                        RETURN thread
                )
                LET recommendations = (
                    FOR thread in followed
                        LIMIT 5
                        LET subRecommendation = (FOR result IN ThreadsView
                            SEARCH ANALYZER(result.Description IN TOKENS(thread.Description, 'text_en'), 'text_en')
                            SORT BM25(result) DESC
                            LIMIT 5
                            RETURN result
                        )
                        RETURN subRecommendation
                )
                LET uniqueRecommendations = (
                    FOR thread in FLATTEN(recommendations)
                        FILTER thread not in followed
                        RETURN DISTINCT thread
                )
                LET mostPopular = (
                    FOR thread IN Threads
                        SORT thread.NumOfFollowers DESC
                        LIMIT 100
                        RETURN thread
                )
                LET fill = (
                    FOR thread in mostPopular
                        FILTER thread not in followed AND thread not in uniqueRecommendations
                        SORT RAND()
                        LIMIT 25
                        RETURN thread
                )
                FOR thread IN SLICE(APPEND(uniqueRecommendations, fill), 0, 25)
                    RETURN thread""";
    }
}
