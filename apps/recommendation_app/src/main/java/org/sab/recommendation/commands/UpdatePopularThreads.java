package org.sab.recommendation.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.TimeoutException;
import com.couchbase.client.java.json.JacksonTransformers;
import com.couchbase.client.java.json.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.recommendation.RecommendationApp;
import org.sab.service.Command;
import org.sab.service.Responder;

public class UpdatePopularThreads extends Command {
    Arango arango;
    @Override
    public String execute(JSONObject request) {
        JSONArray data = new JSONArray();
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            String query = """
                    FOR thread IN %s
                        SORT thread.%s DESC
                        LIMIT 100
                        RETURN thread"""
                    .formatted(RecommendationApp.THREADS_COLLECTION_NAME, RecommendationApp.THREAD_FOLLOWERS);
            ArangoCursor<BaseDocument> cursor = arango.query(RecommendationApp.DB_NAME, query, null);

            cursor.forEachRemaining(document -> {
                JSONObject thread = new JSONObject();
                thread.put(RecommendationApp.THREAD_NAME, document.getKey());
                thread.put(RecommendationApp.THREAD_DESCRIPTION, document.getProperties().get(RecommendationApp.THREAD_DESCRIPTION));
                thread.put(RecommendationApp.THREAD_CREATOR, document.getProperties().get(RecommendationApp.THREAD_CREATOR));
                thread.put(RecommendationApp.THREAD_FOLLOWERS, document.getProperties().get(RecommendationApp.THREAD_FOLLOWERS));
                thread.put(RecommendationApp.THREAD_DATE, document.getProperties().get(RecommendationApp.THREAD_DATE));
                data.put(thread);
            });
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        } finally {
            arango.disconnect();
        }

        if (data.length() != 0) {
            try {
                Couchbase couchbase = Couchbase.getInstance();
                couchbase.connectIfNotConnected();

                JsonObject couchbaseData = JsonObject.create().put(RecommendationApp.THREADS_DATA_KEY, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(RecommendationApp.LISTINGS_BUCKET_NAME, RecommendationApp.LISTINGS_POPULAR_THREADS_KEY, couchbaseData);
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