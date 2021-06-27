package org.sab.notification.commands;

import org.sab.models.user.UserAttributes;
import org.sab.notification.FirestoreConnector;
import org.sab.notification.NotificationApp;
import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RegisterDeviceToken extends CommandWithVerification {
    static final String USERNAME = UserAttributes.USERNAME.toString();
    static final String TOKEN = NotificationApp.TOKEN;
    static final String TOKENS_COLLECTION = NotificationApp.TOKENS_COLLECTION;
    static final String WAIT_UNTIL_REGISTERED = "waitUntilRegistered";

    @Override
    protected String execute() {
        try {
            final String username = body.getString(USERNAME);
            final String token = body.getString(TOKEN);
            final boolean blocking = body.optBoolean(WAIT_UNTIL_REGISTERED, false);
            final FirestoreConnector firestore = FirestoreConnector.getInstance();

            Map<String, Object> document = firestore.readDocument(TOKENS_COLLECTION, username);
            if (document == null)
                newUserToken(firestore, username, token, blocking);
            else
                oldUserToken(firestore, username, token, blocking, (List<String>)document.get("tokens"));

            return Responder.makeMsgResponse("You have successfully registered to the notification service");
        } catch (ExecutionException e) {
            return Responder.makeErrorResponse("Error occurred while fetching data from Firestore", 500).toString();
        } catch (InterruptedException e) {
            return Responder.makeErrorResponse("Data fetching interrupted", 500).toString();
        } catch (NullPointerException e) {
            return Responder.makeErrorResponse("Corrupted documents at Firestore", 500).toString();
        } catch (ClassCastException e) {
            return Responder.makeErrorResponse("Corrupted field at Firestore", 500).toString();
        }
    }

    private void newUserToken(FirestoreConnector firestore, String username, String token, boolean blocking) throws ExecutionException, InterruptedException {
        Map<String, Object> tokenMap = Map.of("tokens", List.of(token));
        firestore.upsertDocument(TOKENS_COLLECTION, username, tokenMap, blocking);
    }

    private void oldUserToken(FirestoreConnector firestore, String username, String token, boolean blocking, List<String> tokens) throws ExecutionException, InterruptedException {
        if (tokens.contains(token))
            return;

        tokens.add(token);
        Map<String, Object> tokenMap = Map.of("tokens", tokens);
        firestore.upsertDocument(TOKENS_COLLECTION, username, tokenMap, blocking);
    }

    @Override
    protected Schema getSchema() {
        final Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        final Attribute token = new Attribute(TOKEN, DataType.STRING, true);
        final Attribute blocking = new Attribute(WAIT_UNTIL_REGISTERED, DataType.BOOLEAN, false);
        return new Schema(List.of(username, token));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }
}
