package org.sab.notification.commands;

import org.sab.notification.FirestoreConnector;

import org.junit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FirestoreTest {

    private static FirestoreConnector firestore = FirestoreConnector.getInstance();
    private static String collectionName = "Test1";
    private static String key = "new";

//    @AfterClass
//    public static void tearDown() {
//        firestore.deleteDocument(collectionName, key);
//    }

    @Test
    public void crudTest() {
        try {
            assertEquals(firestore.documentCount(collectionName), 1);

            Map<String, Object> properties = new HashMap<>(2);
            properties.put("flag", false);
            ArrayList<String> array = new ArrayList<String>(1);
            array.add("hello");
            properties.put("array", array);

            firestore.upsertDocument(collectionName, key, properties);
            assertEquals(firestore.documentCount(collectionName), 1);

            Map<String, Object> document = firestore.readDocument(collectionName, key);
            assertEquals(firestore.documentCount(collectionName), 1);
            assertEquals(document.size(), properties.size());
            for (String field : document.keySet())
                assertEquals(document.get(field), properties.get(field));

            properties.put("flag", true);
            ((ArrayList<String>)properties.get("array")).add("world");

            firestore.upsertDocument(collectionName, key, properties);
            assertEquals(firestore.documentCount(collectionName), 1);

            document = firestore.readDocument(collectionName, key);
            assertEquals(firestore.documentCount(collectionName), 1);
            assertEquals(document.size(), properties.size());
            for (String field : document.keySet())
                assertEquals(document.get(field), properties.get(field));

//            collectionName = "Test";
//
//            firestore.deleteDocument(collectionName, key);
//            assertEquals(firestore.documentCount(collectionName), 0);
//            assertNull(firestore.readDocument(collectionName, key));

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
