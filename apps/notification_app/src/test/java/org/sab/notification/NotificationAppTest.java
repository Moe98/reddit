package org.sab.notification;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.models.NotificationAttributes;
import org.sab.notification.commands.GetNotifications;
import org.sab.notification.commands.RegisterDeviceToken;
import org.sab.notification.commands.SendNotification;
import org.sab.service.validation.HTTPMethod;
import org.sab.tests.TestsUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationAppTest {

    static final long CURRENT_TIME = System.currentTimeMillis();
    static final String USERNAME = "scale-a-bull" + CURRENT_TIME;
    static final String TOKEN = "INSERT YOUR TOKEN HERE";

    static final String NOTIFICATION_TITLE = "Congratulations!";
    static final String NOTIFICATION_BODY = "You have graduated with honors!";

    @Test
    public void T01_registerDeviceInsertsTokenInFirestore() {

        JSONObject body = new JSONObject().put("username", USERNAME).put(NotificationApp.TOKEN, TOKEN);
        JSONObject request = TestsUtils.makeAuthorizedRequest(body, HTTPMethod.POST.toString(), new JSONObject());
        new RegisterDeviceToken().execute(request);
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            FirestoreConnector firestoreConnector = FirestoreConnector.getInstance();
            try {
                Map<String, Object> document = firestoreConnector.readDocument(NotificationApp.TOKENS_COLLECTION, USERNAME);
                List<String> tokens = (List<String>) document.get("tokens");
                assertEquals(TOKEN, tokens.get(tokens.size() - 1));
            } catch (ExecutionException | InterruptedException e) {
                fail(e.getMessage());
            }
        }, 1, TimeUnit.SECONDS);

    }

    @Test
    @Ignore
    public void T02_canSendNotificationToRegisteredUser() {
        final JSONObject body = new JSONObject(
                Map.of(
                        NotificationAttributes.USERS_LIST.getValue(), List.of(USERNAME),
                        NotificationAttributes.TITLE.getValue(), NOTIFICATION_TITLE,
                        NotificationAttributes.NOTIFICATION_BODY.getValue(), NOTIFICATION_BODY)
        );

        JSONObject request = TestsUtils.makeAuthorizedRequest(body, HTTPMethod.PUT.toString(), new JSONObject());
        final JSONObject response = new JSONObject(new SendNotification().execute(request));
        String notificationResult = response.getJSONObject("data").getString("notificationResult");
        final boolean isSuccessful = notificationResult.startsWith("Successfully sent message");
        assertTrue("Should send successfully", isSuccessful);
    }

    @Test
    @Ignore
    public void T03_canRetrieveNotificationFromDatabase() {
        JSONObject request = TestsUtils.makeAuthorizedRequest(null, HTTPMethod.GET.toString(), new JSONObject(), USERNAME);
        JSONObject response = new JSONObject(new GetNotifications().execute(request));
        JSONArray notifications = response.getJSONArray("data");
        JSONObject lastNotification = notifications.getJSONObject(notifications.length() - 1);
        assertEquals(lastNotification.getString("title"), NOTIFICATION_TITLE);
        assertEquals(lastNotification.getString("body"), NOTIFICATION_BODY);
    }

}
