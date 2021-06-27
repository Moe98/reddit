package org.sab.notification.commands;

import com.google.cloud.firestore.CollectionReference;
import org.json.JSONArray;
import org.sab.models.user.UserAttributes;
import org.sab.notification.FirestoreConnector;
import org.sab.notification.NotificationApp;
import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GetNotifications extends CommandWithVerification {

    @Override
    protected String execute() {
        String username = authenticationParams.getString(UserAttributes.USERNAME.toString());
        FirestoreConnector firestoreConnector = FirestoreConnector.getInstance();

        List<Map<String, Object>> notifications = firestoreConnector.readCollection(NotificationApp.getNotificationsCollectionName(username));

        return Responder.makeDataResponse(new JSONArray(notifications)).toString();
    }

    @Override
    protected Schema getSchema() {
        return Schema.emptySchema();
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

}
