package org.sab.recommendation.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JacksonTransformers;
import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.SubThread;
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
        JsonNodeFactory nf = JsonNodeFactory.instance;
        ObjectNode response = nf.objectNode();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

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

            ArrayNode data = nf.arrayNode();
            if (cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    SubThread subThread = new SubThread();
                    subThread.set_key(document.getKey());
                    subThread.setParentThread((String) document.getProperties().get("ParentThread"));
                    subThread.setTitle((String) document.getProperties().get("Title"));
                    subThread.setCreator((String) document.getProperties().get("Creator"));
                    subThread.setLikes((Integer) document.getProperties().get("Likes"));
                    subThread.setDislikes((Integer) document.getProperties().get("Dislikes"));
                    subThread.setContent((String) document.getProperties().get("Content"));
                    subThread.setHasImage((Boolean) document.getProperties().get("HasImage"));
                    subThread.setTime((String) document.getProperties().get("Time"));
                    data.addPOJO(subThread);
                });
                response.set("data", data);
            } else {
                response.set("msg", nf.textNode("No Result"));
                response.set("data", nf.arrayNode());
            }
        } catch (Exception e) {
            response.set("msg", nf.textNode(e.getMessage()));
            response.set("data", nf.arrayNode());
            response.set("statusCode", nf.numberNode(500));
        } finally {
            arango.disconnect(arangoDB);
        }

        if (response.get("data").size() != 0) {
            try {
                couchbase = Couchbase.getInstance();
                cluster = couchbase.connect();

                if (!cluster.buckets().getAllBuckets().containsKey("RecommendedSubThreads")) {
                    couchbase.createBucket(cluster, "RecommendedSubThreads", 50);
                }

                JsonObject object = JsonObject.create().put("listOfSubThreads", JacksonTransformers.stringToJsonArray(response.get("data").toString()));
                couchbase.upsertDocument(cluster, "RecommendedSubThreads", request.getJSONObject("body").getString("username"), object);
                response.set("msg", nf.textNode("Recommended SubThreads Updated Successfully!"));
                response.set("statusCode", nf.numberNode(200));
            } catch (Exception e) {
                response.set("msg", nf.textNode(e.getMessage()));
                response.set("data", nf.arrayNode());
                response.set("statusCode", nf.numberNode(500));
            } finally {
                couchbase.disconnect(cluster);
            }
        }
        return response.toString();
    }
}
