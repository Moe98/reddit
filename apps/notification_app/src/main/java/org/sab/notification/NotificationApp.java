package org.sab.notification;

import java.util.List;

public class NotificationApp {
    public static void main(String[] args) {
        final List<String> registrationTokens = List.of("a", "b", "c");
        final String title = "New Message";
        final String body = "Lorem ipsum";
        final String result = sendNotification(registrationTokens, title, body);
        System.out.println(result);
    }


    public static String sendNotification(List<String> registrationTokens, String title, String body) {
        try {
            return FirebaseMessagingConnector.getInstance().notify(registrationTokens, title, body);
        } catch (GoogleCredentialsLoadingFailedException e) {
            return "Could not init Firebase app: " + e.getMessage() + e.getCause();
        } catch (NotificationSendingFailedException e) {
            return "Could not notify: " + e.getMessage() + e.getCause();
        }
    }
}
