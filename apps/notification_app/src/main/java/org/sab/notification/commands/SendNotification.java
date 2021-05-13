package org.sab.notification.commands;

import org.json.JSONObject;
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
        final Attribute title = new Attribute("title", DataType.STRING, true);
        final Attribute body = new Attribute("notificationBody", DataType.STRING, true);
        final Attribute registrationTokens = new Attribute("registrationTokens", DataType.ARRAY_OF_STRING, true);
        return new Schema(List.of(title, body, registrationTokens));
    }

    @Override
    protected String execute() {
        final List<String> registrationTokens = List.of((String[]) body.get("registrationTokens"));
        final String title = body.getString("title");
        final String notificationBody = body.getString("notificationBody");

        try {
            final String notificationResult = FirebaseMessagingConnector.getInstance().notify(registrationTokens, title, notificationBody);
            final JSONObject resultObject = new JSONObject(Map.of("notificationResult", notificationResult));
            return Responder.makeDataResponse(resultObject).toString();
        } catch (GoogleCredentialsLoadingFailedException e) {
            return Responder.makeErrorResponse("Could not init Firebase app", 500).toString();
        } catch (NotificationSendingFailedException e) {
            return Responder.makeErrorResponse("Could not notify", 500).toString();
        }
    }

    @Override
    protected HTTPMethod getMethodType() {
        throw new UnsupportedOperationException("This command is not a server-command that is available through an HTTP request");
    }
}
