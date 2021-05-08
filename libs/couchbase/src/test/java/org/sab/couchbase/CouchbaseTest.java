package org.sab.couchbase;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import org.junit.*;

import java.util.ArrayList;
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
            couchbase.connect();
            assertTrue(couchbase.isConnected());

            bucketName = "TestBucket";
            bucketSize = 100;
            documentProperties = new HashMap<>();
            documentProperties.put("boolean_field", true);
            documentProperties.put("int_field", 1);
            documentProperties.put("string_field", "helloCouch");

            if (couchbase.bucketExists(bucketName))
                couchbase.dropBucket(bucketName);

            assertFalse(couchbase.bucketExists(bucketName));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        try {
            couchbase.disconnect();
            assertFalse(couchbase.isConnected());
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Before
    public void buildBucket() {
        try {
            couchbase.createBucket(bucketName, bucketSize);
            assertTrue(couchbase.bucketExists(bucketName));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void dropBucket() {
        try {
            couchbase.dropBucket(bucketName);
            assertFalse(couchbase.bucketExists(bucketName));
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

    @Test
    public void query() {
        try {
            JsonObject document1 = JsonObject.create(1);
            JsonObject document2 = JsonObject.create(1);
            JsonObject document3 = JsonObject.create(1);

            document1.put("flag", true);
            document2.put("flag", true);
            document3.put("flag", false);

            couchbase.upsertDocument(bucketName, "query1", document1);
            couchbase.upsertDocument(bucketName, "query2", document2);
            couchbase.upsertDocument(bucketName, "query3", document3);

            QueryResult result = couchbase.query("SELECT * FROM `" + bucketName + "` WHERE `flag` = TRUE;", true);

            ArrayList<JsonObject> resultList = new ArrayList<>(result.rowsAsObject());

            assertEquals(resultList.size(), 2);
            assertTrue(resultList.get(0).getObject(bucketName).getBoolean("flag"));
            assertTrue(resultList.get(1).getObject(bucketName).getBoolean("flag"));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }
}