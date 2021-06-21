package org.sab.couchbase;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.json.JsonObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class CouchbaseTest {
    private static Couchbase couchbase;
    private static String bucketName;
    private static int bucketSize;
    private static HashMap<String, Object> documentProperties;

    @BeforeClass
    public static void setUp() {
        try {
            couchbase = Couchbase.getInstance();
            couchbase.connectIfNotConnected();
            assertTrue(couchbase.isConnected());

            bucketName = "TestBucket";
            bucketSize = 100;
            documentProperties = new HashMap<>();
            documentProperties.put("boolean_field", true);
            documentProperties.put("int_field", 1);
            documentProperties.put("string_field", "helloCouch");

            couchbase.createBucketIfNotExists(bucketName, bucketSize);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        try {
            couchbase.dropBucket(bucketName);
            couchbase.disconnect();
            assertFalse(couchbase.isConnected());
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getInstance() {
        try {
            Couchbase couch1 = Couchbase.getInstance();
            assertSame(couch1, couchbase);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void upsertDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.from(documentProperties);

            couchbase.upsertDocument(bucketName, "upsert", CreatedDocument);
            assertEquals(couchbase.getDocument(bucketName, "upsert"), CreatedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.from(documentProperties);

            couchbase.upsertDocument(bucketName, "get", CreatedDocument);
            JsonObject retrievedDocument = couchbase.getDocument(bucketName, "get");
            assertEquals(CreatedDocument, retrievedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void documentExists() {
        try {
            JsonObject createdDocument = JsonObject.from(documentProperties);
            couchbase.upsertDocument(bucketName, "exists", createdDocument);
            assertTrue(couchbase.documentExists(bucketName, "exists"));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void replaceDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.from(documentProperties);

            couchbase.upsertDocument(bucketName, "replace", CreatedDocument);
            CreatedDocument.put("AnotherKey", "AnotherValue");
            couchbase.replaceDocument(bucketName, "replace", CreatedDocument);
            assertEquals(couchbase.getDocument(bucketName, "replace"), CreatedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void deleteDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.from(documentProperties);

            couchbase.upsertDocument(bucketName, "delete", CreatedDocument);
            couchbase.deleteDocument(bucketName, "delete");
            assertFalse(couchbase.documentExists(bucketName, "delete"));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

}