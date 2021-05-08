package org.sab.recommendation.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.TimeoutException;
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

public class UpdateRecommendedSubThreads extends Command {

    @Override
    public String execute(JSONObject request) {
        JSONArray data = new JSONArray();
        String username;
        try {
            username = request.getJSONObject("body").getString("username");
            if (username.isBlank())
                return Responder.makeErrorResponse("username must not be blank", 400).toString();

            Arango arango = Arango.getInstance();
            arango.connectIfNotConnected();

            // Recommended SubThreads are fetched by 2 ways combined together:
            // 1st, trending SubThreads (5) from a random sample of followed Threads (5).
            // 2nd, trending SubThreads (5) from a random sample of recommended Threads (5).
            // Returning a total of up to (50) recommended SubThreads per fetch.
            String query = """
                    LET followedSample = (
                        FOR thread IN 1..1 OUTBOUND CONCAT('%s/', @username) %s
                            SORT RAND()
                            RETURN thread
                    )
                    LET recommendationsFromFollowed = (
                        FOR thread in followedSample
                            LIMIT 5
                            LET sortedRecommendation = (
                                FOR subThread IN 1..1 OUTBOUND CONCAT('%s/', thread.%s) %s
                                    SORT subThread.%s DESC
                                    LIMIT 100
                                    SORT SUM([subThread.%s, -subThread.%s]) DESC
                                    LIMIT 5
                                    RETURN subThread
                            )
                            RETURN sortedRecommendation
                    )
                    LET recommendedThreads = (
                    """
                    .formatted(RecommendationApp.usersCollectionName,
                            RecommendationApp.userFollowThreadCollectionName,
                            RecommendationApp.threadsCollectionName,
                            RecommendationApp.threadName,
                            RecommendationApp.threadContainSubThreadCollectionName,
                            RecommendationApp.subThreadDate,
                            RecommendationApp.subThreadLikes,
                            RecommendationApp.subThreadDislikes)
                    +
                    UpdateRecommendedThreads.getQuery() +
                    """
                            )
                            LET recommendationsFromRecommendedThreads = (
                                FOR thread IN recommendedThreads
                                    SORT RAND()
                                    LIMIT 5
                                    LET sortedRecommendedThreadsRecommendation = (
                                        FOR subThread IN 1..1 OUTBOUND CONCAT('%s/', thread.%s) %s
                                            SORT subThread.%s DESC
                                            LIMIT 100
                                            SORT SUM([subThread.%s, -subThread.%s]) DESC
                                            LIMIT 5
                                            RETURN subThread
                                    )
                                    RETURN sortedRecommendedThreadsRecommendation
                            )
                            FOR subThread IN SLICE(APPEND(FLATTEN(recommendationsFromFollowed), FLATTEN(recommendationsFromRecommendedThreads)), 0, 50)
                                    RETURN subThread"""
                            .formatted(RecommendationApp.threadsCollectionName,
                                    RecommendationApp.threadName,
                                    RecommendationApp.threadContainSubThreadCollectionName,
                                    RecommendationApp.subThreadDate,
                                    RecommendationApp.subThreadLikes,
                                    RecommendationApp.subThreadDislikes);
            Map<String, Object> bindVars = Collections.singletonMap("username", username);
            ArangoCursor<BaseDocument> cursor = arango.query(RecommendationApp.dbName, query, bindVars);

            cursor.forEachRemaining(document -> {
                JSONObject subThread = new JSONObject();
                subThread.put(RecommendationApp.subThreadId, document.getKey());
                subThread.put(RecommendationApp.subThreadParentThread, document.getProperties().get(RecommendationApp.subThreadParentThread));
                subThread.put(RecommendationApp.subThreadTitle, document.getProperties().get(RecommendationApp.subThreadTitle));
                subThread.put(RecommendationApp.subThreadCreator, document.getProperties().get(RecommendationApp.subThreadCreator));
                subThread.put(RecommendationApp.subThreadLikes, document.getProperties().get(RecommendationApp.subThreadLikes));
                subThread.put(RecommendationApp.subThreadDislikes, document.getProperties().get(RecommendationApp.subThreadDislikes));
                subThread.put(RecommendationApp.subThreadContent, document.getProperties().get(RecommendationApp.subThreadContent));
                subThread.put(RecommendationApp.subThreadHasImage, document.getProperties().get(RecommendationApp.subThreadHasImage));
                subThread.put(RecommendationApp.subThreadDate, document.getProperties().get(RecommendationApp.subThreadDate));
                data.put(subThread);
            });
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }

        if (data.length() != 0) {
            try {
                Couchbase couchbase = Couchbase.getInstance();
                couchbase.connectIfNotConnected();

                JsonObject couchbaseData = JsonObject.create().put(RecommendationApp.subThreadsDataKey, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(RecommendationApp.recommendedSubThreadsBucketName, username, couchbaseData);
            } catch (TimeoutException e) {
                return Responder.makeErrorResponse("Request to Couchbase timed out.", 408).toString();
            } catch (CouchbaseException e) {
                return Responder.makeErrorResponse("Couchbase error: " + e.getMessage(), 500).toString();
            } catch (Exception e) {
                return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
            }
        }
        return Responder.makeDataResponse(data).toString();
    }
}
