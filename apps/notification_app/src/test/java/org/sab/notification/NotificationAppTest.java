package org.sab.notification;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import java.util.List;

@Ignore
public class NotificationAppTest {

    private static final String JAMIE_REG_TOKEN = "not-yet-known";
    private static final String ALEX_REG_TOKEN = "not-yet-known";  

    @Test
    public void shouldSendSingularWithSuccess() {
        final String response = NotificationApp.sendNotification(
                List.of(JAMIE_REG_TOKEN), 
                "Some Title", 
                "Lorem ipsum"
        );

        final boolean isSuccessful = response.startsWith("Successfully sent message: ");
        assertTrue("Should send successfully", isSuccessful);
    }
    
    @Test
    public void shouldSendMultipleWithSuccess() {
        final String response = NotificationApp.sendNotification(
                List.of(JAMIE_REG_TOKEN, ALEX_REG_TOKEN),
                "Some Title", 
                "Lorem ipsum"
        );

        final boolean isSuccessful = response.startsWith("Successfully sent message to ");
        assertTrue("Should send successfully", isSuccessful);
    }
}
