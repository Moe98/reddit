package org.sab.thread.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class ModeratorBansUser extends ThreadCommand {
    private Arango arango;
    private ArangoDB arangoDB;

    public static void main(String[] args) {
        ModeratorBansUser moderatorBansUser = new ModeratorBansUser();
        JSONObject body = new JSONObject();
        body.put(THREAD_NAME, "asmakElRayes7amido");
        body.put(BANNED_USER_ID, "541");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "33366");

        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", "PUT");
        request.put("uriParams", uriParams);

        System.out.println(request);
        System.out.println("=========");

        System.out.println(moderatorBansUser.execute(request));
    }

    @Override
    protected String execute() {
        final String threadName = body.getString(THREAD_NAME);
        final String bannedUserId = body.getString(BANNED_USER_ID);
        final String userId = uriParams.getString(ACTION_MAKER_ID);

        final JSONObject response = new JSONObject();
        String messageResponse = "";

        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB.
            if (!arango.collectionExists(arangoDB, DB_Name, THREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, THREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_THREAD_MOD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_THREAD_MOD_COLLECTION_NAME, true);
            }
            if (!arango.collectionExists(arangoDB, DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME)) {
                arango.createCollection(arangoDB, DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME, true);
            }

            final String threadModeratorEdgeKey = userId + threadName;
            final String bannedUserEdgeKey = bannedUserId + threadName;

            if (!arango.documentExists(arangoDB, DB_Name, USER_THREAD_MOD_COLLECTION_NAME, threadModeratorEdgeKey)) {
                // User isn't a moderator of this thread.
                messageResponse = "You are not a moderator of this thread.";
                return Responder.makeErrorResponse(messageResponse, 401).toString();
            }
            if (arango.documentExists(arangoDB, DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME, bannedUserEdgeKey)) {
                // User is already banned from this thread.
                messageResponse = "User is already banned from this thread.";
                return Responder.makeErrorResponse(messageResponse, 400).toString();
            }

            // The request was made from a moderator of this thread, and the
            // user has not been banned yet from the thread.
            final BaseEdgeDocument userBannedFromThreadEdge = addEdgeFromUserToThread(bannedUserId, threadName, bannedUserEdgeKey);
            arango.createEdgeDocument(arangoDB, DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME, userBannedFromThreadEdge);
            messageResponse = "User has been successfully banned.";
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
            arango.disconnect(arangoDB);
            response.put("msg", messageResponse);
        }

        return Responder.makeDataResponse(response).toString();
    }

    @Override
    protected Schema getSchema() {
        final Attribute threadName = new Attribute(THREAD_NAME, DataType.STRING, true);
        final Attribute bannedUserId = new Attribute(BANNED_USER_ID, DataType.STRING, true);

        return new Schema(List.of(threadName, bannedUserId));
    }
}