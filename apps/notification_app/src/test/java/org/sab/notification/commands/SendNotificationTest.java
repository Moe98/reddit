package org.sab.notification.commands;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.sab.service.validation.HTTPMethod;
import org.sab.tests.TestsUtils;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@Ignore
public class SendNotificationTest {

    private static final String USERNAME = "scale-a-bull";

    @Test
    public void shouldSendSingularWithSuccess() {
        final JSONObject body = new JSONObject(
                Map.of(
                        "usersList", List.of(USERNAME),
                        "title", "Some Title",
                        "notificationBody", "Lorem ipsum"
                )
        );
        JSONObject request = TestsUtils.makeAuthorizedRequest(body, HTTPMethod.PUT.toString(), new JSONObject());
        final JSONObject response = new JSONObject(new SendNotification().execute(request));
        String notificationResult = response.getJSONObject("data").getString("notificationResult");
        final boolean isSuccessful = notificationResult.startsWith("Successfully sent message");
        assertTrue("Should send successfully", isSuccessful);
    }


}
