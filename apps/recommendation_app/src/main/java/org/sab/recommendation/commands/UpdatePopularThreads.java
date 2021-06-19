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

public class UpdatePopularThreads extends RecommendationCommand {
    Arango arango;

    @Override
    public String execute() {
        JSONArray data = new JSONArray();
        try {
            arango = Arango.getInstance();

            String query = """
                    FOR thread IN %s
                        SORT thread.%s DESC
                        LIMIT 100
                        RETURN thread"""
                    .formatted(THREADS_COLLECTION_NAME, THREAD_FOLLOWERS);
            ArangoCursor<BaseDocument> cursor = arango.query(DB_NAME, query, null);

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
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }

        if (data.length() != 0) {
            try {
                Couchbase couchbase = Couchbase.getInstance();
                couchbase.connectIfNotConnected();

                JsonObject couchbaseData = JsonObject.create().put(THREADS_DATA_KEY, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(LISTINGS_BUCKET_NAME, LISTINGS_POPULAR_THREADS_KEY, couchbaseData);
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