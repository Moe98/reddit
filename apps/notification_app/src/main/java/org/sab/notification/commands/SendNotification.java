package org.sab.notification.commands;

import com.google.cloud.Timestamp;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sab.models.NotificationAttributes;
import org.sab.notification.*;
import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SendNotification extends CommandWithVerification {

    @Override
    protected Schema getSchema() {

        final Attribute title = new Attribute(NotificationAttributes.TITLE.getValue(), DataType.STRING, true);
        final Attribute body = new Attribute(NotificationAttributes.NOTIFICATION_BODY.getValue(), DataType.STRING, true);
        final Attribute usersList = new Attribute(NotificationAttributes.USERS_LIST.getValue(), DataType.JSON_ARRAY, true);
        return new Schema(List.of(title, body, usersList));
    }

    @Override
    protected String execute() {

        final List<String> usersList = parseJsonArray(body.getJSONArray(NotificationAttributes.USERS_LIST.getValue()));
        final String title = body.getString(NotificationAttributes.TITLE.getValue());
        final String notificationBody = body.getString(NotificationAttributes.NOTIFICATION_BODY.getValue());
        final FirestoreConnector firestore = FirestoreConnector.getInstance();

        try {
            final String notificationResult = FirebaseMessagingConnector.getInstance().notify(getRegistrationTokens(firestore, usersList), title, notificationBody);
            final JSONObject resultObject = new JSONObject(Map.of("notificationResult", notificationResult));
            insertNotificationToFirestore(firestore, usersList, title, notificationBody);
            return Responder.makeDataResponse(resultObject).toString();
        } catch (GoogleCredentialsLoadingFailedException e) {
            return Responder.makeErrorResponse("Could not init Firebase app", 500).toString();
        } catch (NotificationSendingFailedException e) {
            return Responder.makeErrorResponse("Could not notify", 500).toString();
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

    private List<String> parseJsonArray(JSONArray jsonArray) {
        return jsonArray.toList().stream().map(Object::toString).collect(Collectors.toList());
    }

    private void insertNotificationToFirestore(FirestoreConnector firestore, List<String> usersList, String title, String notificationBody) throws ExecutionException, InterruptedException {
        Map<String, Object> notificationMap = new HashMap<>(3);
        notificationMap.put("title", title);
        notificationMap.put("body", notificationBody);
        notificationMap.put("time", Timestamp.now());

        for (String user : usersList) {
            firestore.createDocumentWithRandomKey(NotificationApp.getNotificationsCollectionName(user), notificationMap);
        }
    }

    /**
     * Retrieves the list of tokens from the firestore db given a userList
     *
     * @param usersList list of usernames
     * @return list of tokens
     */
    private List<String> getRegistrationTokens(FirestoreConnector firestore, List<String> usersList) throws ExecutionException, InterruptedException {
        ArrayList<String> tokens = new ArrayList<>();

        for (String user : usersList) {
            Map<String, Object> document = firestore.readDocument("userTokens", user);
            if (document == null)
                continue;

            tokens.addAll((List<String>) document.get("tokens"));
        }
        return tokens;
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
