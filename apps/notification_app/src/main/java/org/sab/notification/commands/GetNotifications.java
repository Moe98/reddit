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
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GetNotifications extends CommandWithVerification {

    @Override
    protected String execute() {
        String username = authenticationParams.getString(UserAttributes.USERNAME.toString());
        FirestoreConnector firestoreConnector = FirestoreConnector.getInstance();

        CollectionReference collectionReference = firestoreConnector.readCollection(NotificationApp.getNotificationsCollectionName(username));
        ArrayList<Map<String, Object>> notifications = new ArrayList<>();
        collectionReference.listDocuments().forEach(documentReference -> {
            try {
                notifications.add(documentReference.get().get().getData());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

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
