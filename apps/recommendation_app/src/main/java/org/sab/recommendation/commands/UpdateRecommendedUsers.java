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
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.Collections;
import java.util.Map;

public class UpdateRecommendedUsers extends RecommendationCommand {
    Arango arango;

    @Override
    public String execute() {
        JSONArray data = new JSONArray();
        String username;
        try {
            username = authenticationParams.getString(USERNAME);
            arango = Arango.getInstance();

//          First, we acquire the followed users. Based on the acquired results, we acquire the
//          users which followed users follow, Then, these users are filtered and sorted according to
//          a score that is based on the number of the followers of these users that the main user follow.
            String query = """
                    LET followed = (
                        FOR user IN 1..1 OUTBOUND CONCAT('%s/', @username) %s
                            RETURN user
                    )
                    LET blocked = (
                        FOR user IN 1..1 OUTBOUND CONCAT('%s/', @username) %s
                            RETURN user
                    )
                    FOR user IN 2..2 OUTBOUND CONCAT('%s/', @username) %s
                         Filter user._id != CONCAT('%s/', @username) AND user NOT IN followed AND user NOT IN blocked
                         COLLECT friend = user._key WITH COUNT INTO mutual_number
                         SORT mutual_number DESC
                         LIMIT 25
                         RETURN {username:friend}"""
                    .formatted(USERS_COLLECTION_NAME,
                            USER_FOLLOW_USER_COLLECTION_NAME,
                            USERS_COLLECTION_NAME,
                            USER_BLOCK_USER_COLLECTION_NAME,
                            USERS_COLLECTION_NAME,
                            USER_FOLLOW_USER_COLLECTION_NAME,
                            USERS_COLLECTION_NAME);
            Map<String, Object> bindVars = Collections.singletonMap("username", username);
            ArangoCursor<BaseDocument> cursor = arango.query(DB_NAME, query, bindVars);

            cursor.forEachRemaining(document -> data.put(document.getProperties().get("username")));
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500);
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400);
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong: " + e.getMessage(), 500);
        }

        if (data.length() != 0) {
            try {
                Couchbase couchbase = Couchbase.getInstance();
                couchbase.connectIfNotConnected();

                JsonObject couchbaseData = JsonObject.create().put(USERNAMES_DATA_KEY, JacksonTransformers.stringToJsonArray(data.toString()));
                couchbase.upsertDocument(RECOMMENDED_USERS_BUCKET_NAME, username, couchbaseData);
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

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }
}