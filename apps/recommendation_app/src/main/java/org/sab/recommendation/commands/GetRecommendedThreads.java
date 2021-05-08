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

public class GetRecommendedThreads extends Command {

    @Override
    public String execute(JSONObject request) {
        try {
            String username = request.getJSONObject("body").getString("username");
            if (username.isBlank())
                return Responder.makeErrorResponse("username must not be blank", 400).toString();

            Couchbase couchbase = Couchbase.getInstance();
            couchbase.connectIfNotConnected();

            JsonArray result = couchbase.getDocument(RecommendationApp.recommendedThreadsBucketName, username).getArray(RecommendationApp.threadsDataKey);
            try {
                return Responder.makeDataResponse(new JSONArray(result.toString())).toString();
            } catch (JSONException e) {
                return Responder.makeErrorResponse("Failed to create data JSONArray from Couchbase results.", 500).toString();
            }
        } catch (DocumentNotFoundException e) {
            return new UpdateRecommendedThreads().execute(request);
        } catch (TimeoutException e) {
            return Responder.makeErrorResponse("Request to Couchbase timed out.", 408).toString();
        } catch (CouchbaseException e) {
            return Responder.makeErrorResponse("Couchbase error: " + e.getMessage(), 500).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }
    }
}