package org.sab.couchbase;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;
import static org.junit.Assert.*;

public class CouchbaseTest {
    private Couchbase couchbase;
    private Cluster cluster;
    private String bucketName;
    private int bucketSize;
    private HashMap<String, Object> documentProperties;

    @Before
    public void setUp() throws Exception {
        try {
            couchbase = Couchbase.getInstance();
            cluster = couchbase.connect();

            bucketName = "TestBucket";
            bucketSize = 100;
            documentProperties = new HashMap<>();
            documentProperties.put("boolean_field", true);
            documentProperties.put("int_field", 1);
            documentProperties.put("string_field", "helloCouch");

            couchbase.createBucket(cluster,bucketName,bucketSize);
            assertTrue(cluster.buckets().getAllBuckets().containsKey(bucketName));

        } catch (CouchbaseException e){
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        try {

            couchbase.dropBucket(cluster,bucketName);
            assertFalse(cluster.buckets().getAllBuckets().containsKey(bucketName));
            couchbase.disconnect(cluster);
        }catch (CouchbaseException e){
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

//    @Test
//    public void connect() {
//    }
//
//    @Test
//    public void disconnect() {
//    }

    @Test
    public void createBucket() {
        try {
            couchbase.createBucket(cluster,"TestCreateBuacke_2",bucketSize);
            assertTrue(cluster.buckets().getAllBuckets().containsKey("TestCreateBuacke_2"));
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void dropBucket() {
        try {
            cluster.buckets().createBucket(BucketSettings.create("TestDropBucket").ramQuotaMB(bucketSize));
            cluster.query("CREATE PRIMARY INDEX on `default` : `" + "TestDropBucket" + "`;",
                    queryOptions().metrics(true));
            couchbase.dropBucket(cluster,"TestDropBucket");
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void upsertDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.create();
            CreatedDocument.put("Key","Value");
            couchbase.upsertDocument(cluster,bucketName,"TestDocumentKey1",CreatedDocument);
            assertEquals(cluster.bucket(bucketName).defaultCollection().get("TestDocumentKey1").contentAsObject(),CreatedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.create();
            CreatedDocument.put("Key","Value");
            cluster.bucket(bucketName).defaultCollection().upsert("TestDocumentKey2", CreatedDocument);
            JsonObject retreivedDocument = couchbase.getDocument(cluster,bucketName,"TestDocumentKey2");
            assertEquals(cluster.bucket(bucketName).defaultCollection().get("TestDocumentKey2").contentAsObject(),retreivedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void replaceDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.create();
            CreatedDocument.put("Key","Value");
            cluster.bucket(bucketName).defaultCollection().upsert("TestDocumentKey3", CreatedDocument);
            CreatedDocument.put("AnotherKey","AnotherValue");
            couchbase.replaceDocument(cluster,bucketName,"TestDocumentKey3",CreatedDocument);
            assertEquals(cluster.bucket(bucketName).defaultCollection().get("TestDocumentKey3").contentAsObject(),CreatedDocument);
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void deleteDocument() {
        try {
            JsonObject CreatedDocument = JsonObject.create();
            CreatedDocument.put("Key","Value");
            cluster.bucket(bucketName).defaultCollection().upsert("TestDocumentKey4", CreatedDocument);
            couchbase.deleteDocument(cluster,bucketName,"TestDocumentKey4");
            assertFalse(cluster.bucket(bucketName).defaultCollection().exists("TestDocumentKey4").exists());
        } catch (CouchbaseException e) {
            fail(e.getMessage());
        }
    }
}