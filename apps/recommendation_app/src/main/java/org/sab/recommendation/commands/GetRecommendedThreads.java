package org.sab.recommendation.commands;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.error.TimeoutException;
import com.couchbase.client.java.json.JsonArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.sab.couchbase.Couchbase;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

public class GetRecommendedThreads extends RecommendationCommand {

    @Override
    public String execute() {
        try {
            String username = authenticationParams.getString(USERNAME);
            Couchbase couchbase = Couchbase.getInstance();
            couchbase.connectIfNotConnected();

            JsonArray result = couchbase.getDocument(RECOMMENDED_THREADS_BUCKET_NAME, username).getArray(THREADS_DATA_KEY);
            try {
                return Responder.makeDataResponse(new JSONArray(result.toString())).toString();
            } catch (JSONException e) {
                return Responder.makeErrorResponse("Failed to create data JSONArray from Couchbase results.", 500);
            }
        } catch (DocumentNotFoundException e) {
            return new UpdateRecommendedThreads().execute();
        } catch (TimeoutException e) {
            return Responder.makeErrorResponse("Request to Couchbase timed out.", 408);
        } catch (CouchbaseException e) {
            return Responder.makeErrorResponse("Couchbase error: " + e.getMessage(), 500);
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400);
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500);
        }
    }

    @Override
    protected Schema getSchema() {
        return Schema.emptySchema();
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }
}