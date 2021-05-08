package org.sab.couchbase;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import org.junit.*;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class CouchbaseTest {
    private static Couchbase couchbase;
    private static Cluster cluster;
    private static String bucketName;
    private static int bucketSize;
    private static HashMap<String, Object> documentProperties;

    @BeforeClass
    public static void setUp() {
        try {
            couchbase = Couchbase.getInstance();
            cluster = couchbase.connect();

            bucketName = "TestBucket";
            bucketSize = 100;
            documentProperties = new HashMap<>();
            documentProperties.put("boolean_field", true);
            documentProperties.put("int_field", 1);
            documentProperties.put("string_field", "helloCouch");

            if (couchbase.bucketExists(cluster, bucketName))
                couchbase.dropBucket(cluster, bucketName);
            
            assertFalse(couchbase.bucketExists(cluster, bucketName));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        try {
            couchbase.disconnect(cluster);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Before
    public void buildBucket() {
        try {
            couchbase.createBucket(cluster, bucketName, bucketSize);
            assertTrue(couchbase.bucketExists(cluster, bucketName));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void dropBucket() {
        try {
            couchbase.dropBucket(cluster, bucketName);
            assertFalse(couchbase.bucketExists(cluster, bucketName));
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

            couchbase.upsertDocument(cluster, bucketName, "upsert", CreatedDocument);
            assertEquals(cluster.bucket(bucketName).defaultCollection().get("upsert").contentAsObject(), CreatedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.from(documentProperties);

            cluster.bucket(bucketName).defaultCollection().upsert("get", CreatedDocument);
            JsonObject retrievedDocument = couchbase.getDocument(cluster, bucketName, "get");
            assertEquals(cluster.bucket(bucketName).defaultCollection().get("get").contentAsObject(), retrievedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void documentExists() {
        try {
            JsonObject createdDocument = JsonObject.from(documentProperties);
            couchbase.upsertDocument(cluster, bucketName, "exists", createdDocument);
            assertTrue(couchbase.documentExists(cluster, bucketName, "exists"));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void replaceDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.from(documentProperties);

            cluster.bucket(bucketName).defaultCollection().upsert("replace", CreatedDocument);
            CreatedDocument.put("AnotherKey", "AnotherValue");
            couchbase.replaceDocument(cluster, bucketName, "replace", CreatedDocument);
            assertEquals(cluster.bucket(bucketName).defaultCollection().get("replace").contentAsObject(), CreatedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void deleteDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.from(documentProperties);

            cluster.bucket(bucketName).defaultCollection().upsert("delete", CreatedDocument);
            couchbase.deleteDocument(cluster, bucketName, "delete");
            assertFalse(cluster.bucket(bucketName).defaultCollection().exists("delete").exists());
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

            couchbase.upsertDocument(cluster, bucketName, "query1", document1);
            couchbase.upsertDocument(cluster, bucketName, "query2", document2);
            couchbase.upsertDocument(cluster, bucketName, "query3", document3);

            QueryResult result = couchbase.query(cluster, "SELECT * FROM `" + bucketName + "` WHERE `flag` = TRUE;", true);

            ArrayList<JsonObject> resultList = new ArrayList<>(result.rowsAsObject());

            assertEquals(resultList.size(), 2);
            assertTrue(resultList.get(0).getObject(bucketName).getBoolean("flag"));
            assertTrue(resultList.get(1).getObject(bucketName).getBoolean("flag"));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }
}