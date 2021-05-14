package org.sab.notification.commands;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

@Ignore
public class SendNotificationTest {

    private static final String JAMIE_REG_TOKEN = "not-yet-known";
    private static final String ALEX_REG_TOKEN = "not-yet-known";  

    @Test
    public void shouldSendSingularWithSuccess() {
        final JSONObject request = new JSONObject(
                Map.of(
                    "registrationTokens", List.of(JAMIE_REG_TOKEN),
                    "title", "Some Title",
                    "notificationBody", "Lorem ipsum"
                )
        );

        final String response = new SendNotification().execute(request);

        final boolean isSuccessful = response.startsWith("Successfully sent message: ");
        assertTrue("Should send successfully", isSuccessful);
    }
    
    @Test
    public void shouldSendMultipleWithSuccess() {
        final JSONObject request = new JSONObject(
                Map.of(
                        "registrationTokens", List.of(JAMIE_REG_TOKEN, ALEX_REG_TOKEN),
                        "title", "Some Title",
                        "notificationBody", "Lorem ipsum"
                )
        );

        final String response = new SendNotification().execute(request);

        final boolean isSuccessful = response.startsWith("Successfully sent message to ");
        assertTrue("Should send successfully", isSuccessful);
    }
}
