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
import org.sab.recommendation.RecommendationApp;
import org.sab.service.Command;
import org.sab.service.Responder;

import java.util.Collections;
import java.util.Map;

public class UpdateRecommendedUsers extends Command {

    @Override
    public String execute(JSONObject request) {
        JSONArray data = new JSONArray();
        String username;
        try {
            username = request.getJSONObject("body").getString("username");
            if (username.isBlank())
                return Responder.makeErrorResponse("username must not be blank", 400).toString();

            Arango arango = Arango.getInstance();
            arango.connectIfNotConnected();

//          First, we acquire the followed users. Based on the acquired results, we acquire the
//          users which followed users follow, Then, these users are filtered and sorted according to
//          a score that is based on the number of the followers of these users that the main user follow.
            String query = """
                    LET followed = (
                        FOR user IN 1..1 OUTBOUND CONCAT('%s/', @username) %s
                            RETURN user
                    )
                    FOR user IN 2..2 OUTBOUND CONCAT('%s/', @username) %s
                         Filter user._id != CONCAT('%s/', @username) AND user NOT IN followed
                         COLLECT friend = user._key WITH COUNT INTO mutual_number
                         SORT mutual_number DESC
                         LIMIT 25
                         RETURN {username:friend}"""
                    .formatted(RecommendationApp.usersCollectionName,
                            RecommendationApp.userFollowUserCollectionName,
                            RecommendationApp.usersCollectionName,
                            RecommendationApp.userFollowUserCollectionName,
                            RecommendationApp.usersCollectionName);
            Map<String, Object> bindVars = Collections.singletonMap("username", username);
            ArangoCursor<BaseDocument> cursor = arango.query(RecommendationApp.dbName, query, bindVars);

            cursor.forEachRemaining(document -> data.put(document.getProperties().get("username")));
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500).toString();
        }

        if (data.length() != 0) {
            try {
                Couchbase couchbase = Couchbase.getInstance();
                couchbase.connectIfNotConnected();

                JsonObject couchbaseData = JsonObject.create().put(RecommendationApp.usernamesDataKey, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(RecommendationApp.recommendedUsersBucketName, username, couchbaseData);
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