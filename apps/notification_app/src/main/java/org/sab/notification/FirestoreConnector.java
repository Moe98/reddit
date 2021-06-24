package org.sab.notification;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreConnector {
    private static FirestoreConnector instance = null;

    private final Firestore firestore;

    private FirestoreConnector() {
        FirebaseInitializer.initialize();
        firestore = FirestoreClient.getFirestore();
    }

    public static FirestoreConnector getInstance() {
        if (instance == null)
            instance = new FirestoreConnector();
        return instance;
    }

    public void upsertDocument(String collectionName, String key, Map<String, Object> properties) {
        DocumentReference reference = firestore.collection(collectionName).document(key);
        reference.set(properties);
    }

    public Map<String, Object> readDocument(String collectionName, String key) throws ExecutionException, InterruptedException {
        DocumentReference reference = firestore.collection(collectionName).document(key);
        ApiFuture<DocumentSnapshot> document = reference.get();
        DocumentSnapshot snapshot = document.get();

        if (snapshot.exists())
            return snapshot.getData();
        return null;
    }

    public void deleteDocument(String collectionName, String key) {
        DocumentReference reference = firestore.collection(collectionName).document(key);
        reference.delete();
    }

    public int documentCount(String collectionName) throws ExecutionException, InterruptedException {
        return firestore.collection(collectionName).get().get().size();
    }
}
