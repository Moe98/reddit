package org.sab.notification;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.List;
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

    public void upsertDocument(String collectionName, String key, Map<String, Object> properties) throws ExecutionException, InterruptedException {
        upsertDocument(collectionName, key, properties, false);
    }

    public void upsertDocument(String collectionName, String key, Map<String, Object> properties, boolean waitForFuture) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> resultFuture = firestore.collection(collectionName).document(key).set(properties);

        if (waitForFuture)
            resultFuture.get();
    }

    public String createDocumentWithRandomKey(String collectionName, Map<String, Object> properties) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> referenceFuture = firestore.collection(collectionName).add(properties);
        return referenceFuture.get().getId();
    }

    public Map<String, Object> readDocument(String collectionName, String key) throws ExecutionException, InterruptedException {
        DocumentReference reference = firestore.collection(collectionName).document(key);
        ApiFuture<DocumentSnapshot> document = reference.get();
        DocumentSnapshot snapshot = document.get();

        if (snapshot.exists())
            return snapshot.getData();
        return null;
    }

    public void deleteDocument(String collectionName, String key) throws ExecutionException, InterruptedException {
        deleteDocument(collectionName, key, false);
    }

    public void deleteDocument(String collectionName, String key, boolean waitForFuture) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> resultFuture = firestore.collection(collectionName).document(key).delete();

        if (waitForFuture)
            resultFuture.get();
    }

    public int documentCount(String collectionName) throws ExecutionException, InterruptedException {
        return firestore.collection(collectionName).get().get().size();
    }

    public CollectionReference readCollection(String collectionName) {
        return firestore.collection(collectionName);
    }
    
}
