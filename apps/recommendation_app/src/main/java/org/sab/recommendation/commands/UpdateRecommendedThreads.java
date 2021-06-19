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

public class UpdateRecommendedThreads extends RecommendationCommand {
    Arango arango;

    public static String getQuery() {
        // Recommended Threads are fetched by 2 ways, where the 2nd way acts as a filler in-case the 1st way returns
        // in sufficient number of recommendations:
        // 1st, similar (using BM25 Ranking Function) Threads (5) to the latest Threads (5) that the User has followed.
        // 2nd, random popular Threads (25-1stWay(N)).
        // Returning up to 25 total recommended Threads.
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
                .formatted(USERS_COLLECTION_NAME,
                        USER_FOLLOW_THREAD_COLLECTION_NAME,
                        USER_FOLLOW_THREAD_DATE,
                        getViewName(THREADS_COLLECTION_NAME),
                        THREAD_DESCRIPTION,
                        THREAD_DESCRIPTION,
                        THREADS_COLLECTION_NAME,
                        THREAD_FOLLOWERS);
    }

    @Override
    public String execute() {
        JSONArray data = new JSONArray();
        String username;
        try {
            username = authenticationParams.getString(USERNAME);
            arango = Arango.getInstance();

            Map<String, Object> bindVars = Collections.singletonMap("username", username);
            ArangoCursor<BaseDocument> cursor = arango.query(DB_NAME, getQuery(), bindVars);

            cursor.forEachRemaining(document -> {
                JSONObject thread = new JSONObject();
                thread.put(THREAD_NAME, document.getKey());
                thread.put(THREAD_DESCRIPTION, document.getProperties().get(THREAD_DESCRIPTION));
                thread.put(THREAD_CREATOR, document.getProperties().get(THREAD_CREATOR));
                thread.put(THREAD_FOLLOWERS, document.getProperties().get(THREAD_FOLLOWERS));
                thread.put(THREAD_DATE, document.getProperties().get(THREAD_DATE));
                data.put(thread);
            });
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500);
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400);
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }

        if (data.length() != 0) {
            try {
                Couchbase couchbase = Couchbase.getInstance();
                couchbase.connectIfNotConnected();

                JsonObject couchbaseData = JsonObject.create().put(THREADS_DATA_KEY, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(RECOMMENDED_THREADS_BUCKET_NAME, username, couchbaseData);
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