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

public class GetPopularThreads extends RecommendationCommand {

    @Override
    public String execute() {
        try {
            Couchbase couchbase = Couchbase.getInstance();
            couchbase.connectIfNotConnected();

            JsonArray result = couchbase.getDocument(LISTINGS_BUCKET_NAME, LISTINGS_POPULAR_THREADS_KEY).getArray(THREADS_DATA_KEY);
            return Responder.makeDataResponse(new JSONArray(result.toString())).toString();
        } catch (DocumentNotFoundException e) {
            return new UpdatePopularThreads().execute();
        } catch (TimeoutException e) {
            return Responder.makeErrorResponse("Request to Couchbase timed out.", 408);
        } catch (CouchbaseException e) {
            return Responder.makeErrorResponse("Couchbase error: " + e.getMessage(), 500);
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Failed to create data JSONArray from Couchbase results.", 500);
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
}