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
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.Collections;
import java.util.Map;

public class UpdateRecommendedSubThreads extends RecommendationCommand {
    Arango arango;

    @Override
    public String execute() {
        JSONArray data = new JSONArray();
        String username;
        try {
            username = authenticationParams.getString(USERNAME);
            arango = Arango.getInstance();

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
                                FOR subThread IN 1..1 OUTBOUND CONCAT('%s/', thread._key) %s
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
                    .formatted(USERS_COLLECTION_NAME,
                            USER_FOLLOW_THREAD_COLLECTION_NAME,
                            THREADS_COLLECTION_NAME,
                            THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME,
                            SUB_THREAD_DATE,
                            SUB_THREAD_LIKES,
                            SUB_THREAD_DISLIKES)
                    +
                    UpdateRecommendedThreads.getQuery() +
                    """
                            )
                            LET recommendationsFromRecommendedThreads = (
                                FOR thread IN recommendedThreads
                                    SORT RAND()
                                    LIMIT 5
                                    LET sortedRecommendedThreadsRecommendation = (
                                        FOR subThread IN 1..1 OUTBOUND CONCAT('%s/', thread._key) %s
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
                            .formatted(THREADS_COLLECTION_NAME,
                                    THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME,
                                    SUB_THREAD_DATE,
                                    SUB_THREAD_LIKES,
                                    SUB_THREAD_DISLIKES);
            Map<String, Object> bindVars = Collections.singletonMap("username", username);
            ArangoCursor<BaseDocument> cursor = arango.query(DB_NAME, query, bindVars);

            cursor.forEachRemaining(document -> {
                JSONObject subThread = new JSONObject();
                subThread.put(SUB_THREAD_ID, document.getKey());
                subThread.put(SUB_THREAD_PARENT_THREAD, document.getProperties().get(SUB_THREAD_PARENT_THREAD));
                subThread.put(SUB_THREAD_TITLE, document.getProperties().get(SUB_THREAD_TITLE));
                subThread.put(SUB_THREAD_CREATOR, document.getProperties().get(SUB_THREAD_CREATOR));
                subThread.put(SUB_THREAD_LIKES, document.getProperties().get(SUB_THREAD_LIKES));
                subThread.put(SUB_THREAD_DISLIKES, document.getProperties().get(SUB_THREAD_DISLIKES));
                subThread.put(SUB_THREAD_CONTENT, document.getProperties().get(SUB_THREAD_CONTENT));
                subThread.put(SUB_THREAD_HAS_IMAGE, document.getProperties().get(SUB_THREAD_HAS_IMAGE));
                subThread.put(SUB_THREAD_DATE, document.getProperties().get(SUB_THREAD_DATE));
                data.put(subThread);
            });
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500);
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400);
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500);
        }

        if (data.length() != 0) {
            try {
                Couchbase couchbase = Couchbase.getInstance();
                couchbase.connectIfNotConnected();

                JsonObject couchbaseData = JsonObject.create().put(SUB_THREADS_DATA_KEY, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(RECOMMENDED_SUB_THREADS_BUCKET_NAME, username, couchbaseData);
            } catch (TimeoutException e) {
                return Responder.makeErrorResponse("Request to Couchbase timed out.", 408);
            } catch (CouchbaseException e) {
                return Responder.makeErrorResponse("Couchbase error: " + e.getMessage(), 500);
            } catch (Exception e) {
                return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500);
            }
        }
        return Responder.makeDataResponse(data).toString();
    }

    @Override
    protected Schema getSchema() {
        return Schema.emptySchema();
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }
}
