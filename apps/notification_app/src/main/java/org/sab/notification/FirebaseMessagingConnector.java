package org.sab.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;

import java.io.IOException;
import java.util.List;

public class FirebaseMessagingConnector {

    private static FirebaseMessagingConnector instance = null;

    private final FirebaseMessaging firebaseMessaging;

    private FirebaseMessagingConnector() throws IOException {
        final FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault()).build();
        FirebaseApp.initializeApp(options);
        this.firebaseMessaging = FirebaseMessaging.getInstance();
    }

    public static FirebaseMessagingConnector getInstance() throws GoogleCredentialsLoadingFailedException {
        if (instance == null) {
            FirebaseMessagingConnector temp;
            try {
                temp = new FirebaseMessagingConnector();
            } catch (IOException e) {
                throw new GoogleCredentialsLoadingFailedException("Could not load credentials.", e);
            }
            instance = temp;
        }
        return instance;
    }

    private Notification createNotification(String title, String body) {
        return Notification.builder().setTitle(title).setBody(body).build();
    }

    private String notifySingleToken(String registrationToken, Notification notification)
            throws NotificationSendingFailedException {
        final Message message = Message.builder().setNotification(notification).setToken(registrationToken).build();
        final String response;
        try {
            response = firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new NotificationSendingFailedException("Sending failed ", e);
        }
        return "Successfully sent message: " + response;
    }

    public String notify(List<String> registrationTokens, String title, String body)
            throws NotificationSendingFailedException {
        final Notification notification = createNotification(title, body);

        if(registrationTokens.isEmpty()) {
            return "No tokens to send to!";
        }

        if (registrationTokens.size() == 1) {
            return notifySingleToken(registrationTokens.get(0), notification);
        }

        final MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(registrationTokens)
                .build();
        final BatchResponse response;
        try {
            response = firebaseMessaging.sendMulticast(message);
        } catch (FirebaseMessagingException e) {
            throw new NotificationSendingFailedException("Sending failed ", e);
        }

        return "Successfully sent message to " + response.getSuccessCount() + " out of " + registrationTokens.size();
    }
}
