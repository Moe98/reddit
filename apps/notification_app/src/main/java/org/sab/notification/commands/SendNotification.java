package org.sab.notification.commands;

import org.json.JSONObject;
import org.sab.models.NotificationAttributes;
import org.sab.notification.FirebaseMessagingConnector;
import org.sab.notification.GoogleCredentialsLoadingFailedException;
import org.sab.notification.NotificationSendingFailedException;
import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.Map;

public class SendNotification extends CommandWithVerification {

    @Override
    protected Schema getSchema() {

        final Attribute title = new Attribute(NotificationAttributes.TITLE.getValue(), DataType.STRING, true);
        final Attribute body = new Attribute(NotificationAttributes.NOTIFICATION_BODY.getValue(), DataType.STRING, true);
        final Attribute registrationTokens = new Attribute(NotificationAttributes.USERS_LIST.getValue(), DataType.ARRAY_OF_STRING, true);
        return new Schema(List.of(title, body, registrationTokens));
    }

    @Override
    protected String execute() {
        
        final List<String> usersList = List.of((String[]) body.get(NotificationAttributes.USERS_LIST.getValue()));
        final String title = body.getString(NotificationAttributes.TITLE.getValue());
        final String notificationBody = body.getString(NotificationAttributes.NOTIFICATION_BODY.getValue());

        try {
            final String notificationResult = FirebaseMessagingConnector.getInstance().notify(getRegistrationTokens(usersList), title, notificationBody);
            final JSONObject resultObject = new JSONObject(Map.of("notificationResult", notificationResult));
            return Responder.makeDataResponse(resultObject).toString();
        } catch (GoogleCredentialsLoadingFailedException e) {
            return Responder.makeErrorResponse("Could not init Firebase app", 500).toString();
        } catch (NotificationSendingFailedException e) {
            return Responder.makeErrorResponse("Could not notify", 500).toString();
        }
    }

    /**
     * Retrieves the list of tokens from the firestore db given a userList
     * @param usersList list of usernames
     * @return list of tokens
     */
    private List<String> getRegistrationTokens(List<String> usersList) {
        // TODO
        throw new UnsupportedOperationException();
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
