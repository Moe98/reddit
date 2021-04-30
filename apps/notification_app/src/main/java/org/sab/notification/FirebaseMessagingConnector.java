package org.sab.notification;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

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
            FirebaseMessagingConnector temp = null;
            try {
                temp = new FirebaseMessagingConnector();
            } catch (IOException e) {
                throw new GoogleCredentialsLoadingFailedException("Could not load credentials.", e);
            }
            instance = temp;
        }
        return instance;
    }
}
