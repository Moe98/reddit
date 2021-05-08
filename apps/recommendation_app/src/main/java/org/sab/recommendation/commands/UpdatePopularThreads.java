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
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.recommendation.RecommendationApp;
import org.sab.service.Command;
import org.sab.service.Responder;

public class UpdatePopularThreads extends Command {
    private Arango arango;
    private Couchbase couchbase;
    private Cluster cluster;

    @Override
    public String execute(JSONObject request) {
        JSONArray data = new JSONArray();
        try {
            arango = Arango.getInstance();
            if (!arango.isConnected())
                arango.connect();

            String query = """
                    FOR thread IN %s
                        SORT thread.%s DESC
                        LIMIT 100
                        RETURN thread"""
                    .formatted(RecommendationApp.threadsCollectionName, RecommendationApp.threadFollowers);
            ArangoCursor<BaseDocument> cursor = arango.query(RecommendationApp.dbName, query, null);

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
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }

        if (data.length() != 0) {
            try {
                couchbase = Couchbase.getInstance();
                if (!couchbase.isConnected())
                    couchbase.connect();

                JsonObject couchbaseData = JsonObject.create().put(RecommendationApp.threadsDataKey, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(RecommendationApp.listingsBucketName, RecommendationApp.listingsPopularThreadsKey, couchbaseData);
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