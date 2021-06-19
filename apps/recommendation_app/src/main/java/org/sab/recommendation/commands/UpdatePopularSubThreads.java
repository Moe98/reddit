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
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

public class UpdatePopularSubThreads extends RecommendationCommand {
    Arango arango;

    @Override
    public String execute() {
        JSONArray data = new JSONArray();
        try {
            arango = Arango.getInstance();

            String query = """
                    FOR subThread IN %s
                        SORT subThread.%s DESC
                        LIMIT 200
                        SORT SUM([subThread.%s, subThread.%s]) DESC
                        LIMIT 100
                        RETURN subThread"""
                    .formatted(SUB_THREADS_COLLECTION_NAME,
                            SUB_THREAD_DATE,
                            SUB_THREAD_LIKES,
                            SUB_THREAD_DISLIKES);
            ArangoCursor<BaseDocument> cursor = arango.query(DB_NAME, query, null);

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
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }

        if (data.length() != 0) {
            try {
                Couchbase couchbase = Couchbase.getInstance();
                couchbase.connectIfNotConnected();

                JsonObject couchbaseData = JsonObject.create().put(SUB_THREADS_DATA_KEY, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(LISTINGS_BUCKET_NAME, LISTINGS_POPULAR_SUB_THREADS_KEY, couchbaseData);
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
}