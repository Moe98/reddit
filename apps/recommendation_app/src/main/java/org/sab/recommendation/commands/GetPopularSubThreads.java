package org.sab.recommendation.commands;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.error.TimeoutException;
import com.couchbase.client.java.json.JsonArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sab.couchbase.Couchbase;
import org.sab.recommendation.RecommendationApp;
import org.sab.service.Command;
import org.sab.service.Responder;

public class GetPopularSubThreads extends Command {

    @Override
    public String execute(JSONObject request) {
        try {
            Couchbase couchbase = Couchbase.getInstance();
            couchbase.connectIfNotConnected();

            JsonArray result = couchbase.getDocument(RecommendationApp.listingsBucketName, RecommendationApp.listingsPopularSubThreadsKey).getArray(RecommendationApp.subThreadsDataKey);
            return Responder.makeDataResponse(new JSONArray(result.toString())).toString();
        } catch (DocumentNotFoundException e) {
            return new UpdatePopularSubThreads().execute(null);
        } catch (TimeoutException e) {
            return Responder.makeErrorResponse("Request to Couchbase timed out.", 408).toString();
        } catch (CouchbaseException e) {
            return Responder.makeErrorResponse("Couchbase error: " + e.getMessage(), 500).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Failed to create data JSONArray from Couchbase results.", 500).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }
    }
}