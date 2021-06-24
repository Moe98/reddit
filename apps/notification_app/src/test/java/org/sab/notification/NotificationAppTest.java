package org.sab.notification;

import org.json.JSONObject;
import org.junit.Test;
import org.sab.notification.commands.RegisterDeviceToken;
import org.sab.service.validation.HTTPMethod;
import org.sab.tests.TestsUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NotificationAppTest {
    @Test
    public void registerDeviceInsertsTokenInFirestore() {
        String username = "scale-a-bull";
        String token = "token" + System.currentTimeMillis();
        JSONObject body = new JSONObject().put("username", username).put(NotificationApp.TOKEN, token);
        JSONObject request = TestsUtils.makeAuthorizedRequest(body, HTTPMethod.POST.toString(), new JSONObject());
        new RegisterDeviceToken().execute(request);
        FirestoreConnector firestoreConnector = FirestoreConnector.getInstance();
        try {
            Map<String, Object> document = firestoreConnector.readDocument(NotificationApp.TOKENS_COLLECTION, username);
            List<String> tokens = (List<String>) document.get("tokens");
            assertEquals(token, tokens.get(tokens.size() - 1));
        } catch (ExecutionException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

}
