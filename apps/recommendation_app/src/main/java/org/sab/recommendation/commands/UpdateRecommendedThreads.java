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
import org.sab.models.Thread;
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
        JsonNodeFactory nf = JsonNodeFactory.instance;
        ObjectNode response = nf.objectNode();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            Map<String, Object> bindVars = Collections.singletonMap("username", "Users/" + request.getJSONObject("body").getString("username"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), getQuery(), bindVars);

            ArrayNode data = nf.arrayNode();
            if (cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    Thread thread = new Thread();
                    thread.setName(document.getKey());
                    thread.setDescription((String) document.getProperties().get("Description"));
                    thread.setCreator((String) document.getProperties().get("Creator"));
                    thread.setNumOfFollowers((Integer) document.getProperties().get("NumOfFollowers"));
                    thread.setDateCreated((String) document.getProperties().get("DateCreated"));
                    data.addPOJO(thread);
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

                if (!couchbase.bucketExists(cluster, "RecommendedThreads")) {
                    couchbase.createBucket(cluster, "RecommendedThreads", 100);
                }

                JsonObject object = JsonObject.create().put("listOfThreads", JacksonTransformers.stringToJsonArray(response.get("data").toString()));
                couchbase.upsertDocument(cluster, "RecommendedThreads", request.getJSONObject("body").getString("username"), object);
                response.set("msg", nf.textNode("Recommended Threads Updated Successfully!"));
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
