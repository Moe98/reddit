package org.sab.couchbase;


import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.bucket.BucketManager;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.query.QueryResult;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;

public class Couchbase {

    Cluster cluster;
    BucketManager bucketManager;

    public void connect(String connectionString, String username, String password){
        cluster = Cluster.connect(connectionString, username, password);
        bucketManager = cluster.buckets();
    }

    public void createBucket(String bucketName, int ramQuotaMB) {
        bucketManager.createBucket(
        BucketSettings.create(bucketName).ramQuotaMB(ramQuotaMB)
        );
        try {
            cluster.query("CREATE PRIMARY INDEX on `default` : `"+bucketName+"`;",
                    queryOptions().metrics(true));
        } catch (CouchbaseException ex) {
            ex.printStackTrace();
        }
    }

    public Collection getCollection(String bucketName){
        Bucket bucket = cluster.bucket(bucketName);
        Collection collection = bucket.defaultCollection();
        return collection;
    }

    public void upsertDocument(Collection collection , String documentKey , String listName, JsonArray listOfObjects){
        JsonObject threadsContent = JsonObject.create().put(listName, listOfObjects);
        MutationResult threadsResult = collection.upsert(documentKey, threadsContent);
    }

    public void getDocument(Collection collection, String documentKey){
        try {
            GetResult getResult = collection.get(documentKey);
            JsonObject resultsObject = getResult.contentAsObject();
            System.out.println(resultsObject);
        } catch (DocumentNotFoundException ex) {
            System.err.println("Document with the given id not found");
        } catch (CouchbaseException ex) {
            System.err.println("Something else happened: " + ex);
        }
    }

    public QueryResult query(String queryText){
        QueryResult result = null;
        try {
            result = cluster.query(queryText, queryOptions().metrics(true));
            System.out.println("Reported execution time: " + result.metaData().metrics().get().executionTime());
            for (JsonObject row : result.rowsAsObject()) {
                System.out.println("Found row: " + row);
            }
        } catch (CouchbaseException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        Couchbase conn = new Couchbase();
        conn.connect("127.0.0.1", "Administrator", "123456");

        conn.createBucket("TestBucket",100);

        Collection testCollection = conn.getCollection("TestBucket");

        conn.upsertDocument(
                testCollection,"testUser1", "listOfUsers",
                JsonArray.create().add("userX").add("userY")
        );

        conn.getDocument(testCollection, "testUser1");

        conn.query("select * from `TestBucket` USE KEYS \"testUser1\" limit 10");






    }




}
