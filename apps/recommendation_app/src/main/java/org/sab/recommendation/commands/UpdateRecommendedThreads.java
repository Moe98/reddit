package org.sab.recommendation.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.TimeoutException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JacksonTransformers;
import com.couchbase.client.java.json.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.recommendation.RecommendationApp;
import org.sab.service.Command;
import org.sab.service.Responder;

import java.util.Collections;
import java.util.Map;

public class UpdateRecommendedThreads extends Command {
    private Arango arango;
    private ArangoDB arangoDB;
    private Couchbase couchbase;
    private Cluster cluster;

    public static String getQuery() {
        return """
                LET followed = (
                    FOR thread, edge IN 1..1 OUTBOUND CONCAT('%s/', @username) %s
                        SORT edge.%s DESC
                        RETURN thread
                )
                LET recommendations = (
                    FOR thread IN followed
                        LIMIT 5
                        LET subRecommendation = (
                            FOR result IN %s
                                SEARCH ANALYZER(result.%s IN TOKENS(thread.%s, 'text_en'), 'text_en')
                                SORT BM25(result) DESC
                                LIMIT 5
                                RETURN result
                        )
                        RETURN subRecommendation
                )
                LET uniqueRecommendations = (
                    FOR thread IN FLATTEN(recommendations)
                        FILTER thread NOT IN followed
                        RETURN DISTINCT thread
                )
                LET mostPopular = (
                    FOR thread IN %s
                        SORT thread.%s DESC
                        LIMIT 100
                        RETURN thread
                )
                LET fill = (
                    FOR thread IN mostPopular
                        FILTER thread NOT IN followed AND thread NOT IN uniqueRecommendations
                        SORT RAND()
                        LIMIT 25
                        RETURN thread
                )
                FOR thread IN SLICE(APPEND(uniqueRecommendations, fill), 0, 25)
                    RETURN thread"""
                .formatted(RecommendationApp.usersCollectionName,
                        RecommendationApp.userFollowThreadCollectionName,
                        RecommendationApp.userFollowThreadDate,
                        RecommendationApp.getViewName(RecommendationApp.threadsCollectionName),
                        RecommendationApp.threadDescription,
                        RecommendationApp.threadDescription,
                        RecommendationApp.threadsCollectionName,
                        RecommendationApp.threadFollowers);
    }

    @Override
    public String execute(JSONObject request) {
        JSONArray data = new JSONArray();
        String username;
        try {
            username = request.getJSONObject("body").getString("username");
            if (username.isBlank())
                return Responder.makeErrorResponse("username must not be blank", 400).toString();

            arango = Arango.getInstance();
            arangoDB = arango.connect();

            Map<String, Object> bindVars = Collections.singletonMap("username", username);
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, RecommendationApp.dbName, getQuery(), bindVars);

            cursor.forEachRemaining(document -> {
                JSONObject thread = new JSONObject();
                thread.put(RecommendationApp.threadName, document.getKey());
                thread.put(RecommendationApp.threadDescription, document.getProperties().get(RecommendationApp.threadDescription));
                thread.put(RecommendationApp.threadCreator, document.getProperties().get(RecommendationApp.threadCreator));
                thread.put(RecommendationApp.threadFollowers, document.getProperties().get(RecommendationApp.threadFollowers));
                thread.put(RecommendationApp.threadDate, document.getProperties().get(RecommendationApp.threadDate));
                data.put(thread);
            });
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        } finally {
            if (arango != null)
                arango.disconnect(arangoDB);
        }

        if (data.length() != 0) {
            try {
                couchbase = Couchbase.getInstance();
                cluster = couchbase.connect();

                JsonObject couchbaseData = JsonObject.create().put(RecommendationApp.threadsDataKey, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(cluster, RecommendationApp.recommendedThreadsBucketName, username, couchbaseData);
            } catch (TimeoutException e) {
                return Responder.makeErrorResponse("Request to Couchbase timed out.", 408).toString();
            } catch (CouchbaseException e) {
                return Responder.makeErrorResponse("Couchbase error: " + e.getMessage(), 500).toString();
            } catch (Exception e) {
                return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
            } finally {
                if (couchbase != null)
                    couchbase.disconnect(cluster);
            }
        }
        return Responder.makeDataResponse(data).toString();
    }
}