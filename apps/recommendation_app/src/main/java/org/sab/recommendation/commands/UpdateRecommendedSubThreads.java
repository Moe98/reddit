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

public class UpdateRecommendedSubThreads extends Command {
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
                arango.createView(arangoDB, System.getenv("ARANGO_DB"), "ThreadsView", "Threads", new String[]{"_key", "Description"});
            }
            if (!arangoDB.db(System.getenv("ARANGO_DB")).view("SubThreadsView").exists()) {
                arango.createView(arangoDB, System.getenv("ARANGO_DB"), "SubThreadsView", "SubThreads", new String[]{"Title", "Content"});
            }

            String query = """
                    LET followedSample = (
                        FOR thread IN 1..1 OUTBOUND @username UserFollowThread
                            SORT RAND()
                            RETURN thread
                    )
                    LET recommendationsFromFollowed = (
                        FOR thread in followedSample
                            LIMIT 5
                            LET sortedRecommendation = (
                                FOR subThread IN 1..1 OUTBOUND CONCAT('Threads/', thread._key) ThreadContainSubThread
                                    SORT subThread.Time DESC
                                    LIMIT 100
                                    SORT SUM([subThread.Likes, -subThread.Dislikes]) DESC
                                    LIMIT 5
                                    RETURN subThread
                            )
                            RETURN sortedRecommendation
                    )
                    LET recommendedThreads = (
                    """ +
                    UpdateRecommendedThreads.getQuery() +
                    """
                            )
                            LET recommendationsFromRecommendedThreads = (
                                FOR thread IN recommendedThreads
                                    SORT RAND()
                                    LIMIT 5
                                    LET sortedRecommendedThreadsRecommendation = (
                                        FOR subThread IN 1..1 OUTBOUND CONCAT('Threads/', thread._key) ThreadContainSubThread
                                            SORT subThread.Time DESC
                                            LIMIT 100
                                            SORT SUM([subThread.Likes, -subThread.Dislikes]) DESC
                                            LIMIT 5
                                            RETURN subThread
                                    )
                                    RETURN sortedRecommendedThreadsRecommendation
                            )
                            FOR subThread IN SLICE(APPEND(FLATTEN(recommendationsFromFollowed), FLATTEN(recommendationsFromRecommendedThreads)), 0, 50)
                                    RETURN subThread""";
            Map<String, Object> bindVars = Collections.singletonMap("username", "Users/" + request.getJSONObject("body").getString("username"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, bindVars);

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

                if (!couchbase.bucketExists(cluster, "RecommendedSubThreads")) {
                    couchbase.createBucket(cluster, "RecommendedSubThreads", 100);
                }

                JsonObject object = JsonObject.create().put("listOfSubThreads", JacksonTransformers.stringToJsonArray(response.getJSONArray("data").toString()));
                couchbase.upsertDocument(cluster, "RecommendedSubThreads", request.getJSONObject("body").getString("username"), object);
                response.put("msg", "Recommended SubThreads Updated Successfully!");
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
