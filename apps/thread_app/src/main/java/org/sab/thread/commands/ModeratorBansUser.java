package org.sab.thread.commands;

import com.arangodb.entity.BaseEdgeDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;

public class ModeratorBansUser extends ThreadCommand {

    public static void main(String[] args) {
        ModeratorBansUser moderatorBansUser = new ModeratorBansUser();
        JSONObject body = new JSONObject();
        body.put(THREAD_NAME, "asmakElRayes7amido");
        body.put(BANNED_USER_ID, "117690");

        JSONObject uriParams = new JSONObject();
        uriParams.put(ACTION_MAKER_ID, "32930");

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
            Arango arango = Arango.getInstance();

            // TODO: System.getenv("ARANGO_DB") instead of writing the DB.
            if (!arango.collectionExists(DB_Name, THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, THREAD_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_COLLECTION_NAME, false);
            }
            if (!arango.collectionExists(DB_Name, USER_MOD_THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_MOD_THREAD_COLLECTION_NAME, true);
            }
            if (!arango.collectionExists(DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME)) {
                arango.createCollection(DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME, true);
            }

            if(!arango.documentExists(DB_Name, THREAD_COLLECTION_NAME, threadName)) {
                messageResponse = "This thread does not exist.";
                return Responder.makeErrorResponse(messageResponse, 400).toString();
            }

            // TODO what if this user is a mod??
            if(!checkUserExists(arango, bannedUserId)){
                messageResponse = "The user you are trying to ban does not exist.";
                return Responder.makeErrorResponse(messageResponse, 400).toString();
            }

            final String threadModeratorEdgeKey =  arango.getSingleEdgeId(DB_Name,
                    USER_MOD_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + userId,
                    THREAD_COLLECTION_NAME + "/" + threadName);
            final String bannedUserEdgeKey = arango.getSingleEdgeId(DB_Name,
                    USER_BANNED_FROM_THREAD_COLLECTION_NAME,
                    USER_COLLECTION_NAME + "/" + bannedUserId,
                    THREAD_COLLECTION_NAME + "/" + threadName);

            if (threadModeratorEdgeKey.equals("")) {
                // User isn't a moderator of this thread.
                messageResponse = "You are not a moderator of this thread.";
                return Responder.makeErrorResponse(messageResponse, 401).toString();
            }
            if (!bannedUserEdgeKey.equals("")) {
                // User is already banned from this thread.
                messageResponse = "User is already banned from this thread.";
                return Responder.makeErrorResponse(messageResponse, 400).toString();
            }

            // The request was made from a moderator of this thread, and the
            // user has not been banned yet from the thread.
            final BaseEdgeDocument userBannedFromThreadEdge = addEdgeFromUserToThread(bannedUserId, threadName);
            arango.createEdgeDocument(DB_Name, USER_BANNED_FROM_THREAD_COLLECTION_NAME, userBannedFromThreadEdge);
            messageResponse = "User has been successfully banned.";
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        } finally {
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