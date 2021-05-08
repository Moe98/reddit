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

public class UpdatePopularSubThreads extends Command {
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
                    FOR subThread IN %s
                        SORT subThread.%s DESC
                        LIMIT 200
                        SORT SUM([subThread.%s, subThread.%s]) DESC
                        LIMIT 100
                        RETURN subThread"""
                    .formatted(RecommendationApp.subThreadsCollectionName,
                            RecommendationApp.subThreadDate,
                            RecommendationApp.subThreadLikes,
                            RecommendationApp.subThreadDislikes);
            ArangoCursor<BaseDocument> cursor = arango.query(RecommendationApp.dbName, query, null);

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
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }

        if (data.length() != 0) {
            try {
                couchbase = Couchbase.getInstance();
                cluster = couchbase.connect();

                JsonObject couchbaseData = JsonObject.create().put(RecommendationApp.subThreadsDataKey, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(cluster, RecommendationApp.listingsBucketName, RecommendationApp.listingsPopularSubThreadsKey, couchbaseData);
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