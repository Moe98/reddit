package org.sab.recommendation;


import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentExistsException;
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

import static com.couchbase.client.java.kv.ReplaceOptions.replaceOptions;
import static com.couchbase.client.java.query.QueryOptions.queryOptions;


public class App {
    public static void testUpsert(Collection threadsCollection, Collection usersCollection){
        // Create an JSON array of recommended items, put it in a JSON object "listOfThreads/Users/Subthreads", and upsert (insert but allows overwriting)
        JsonArray threads = JsonArray.create().add("thread1").add("thread2");
        JsonObject threadsContent = JsonObject.create().put("listOfThreads", threads);
        MutationResult threadsResult = threadsCollection.upsert("hamada", threadsContent);

        JsonArray users = JsonArray.create().add("user1").add("user2");
        JsonObject usersContent = JsonObject.create().put("listOfUsers", users);
        MutationResult usersResult = usersCollection.upsert("hamada", usersContent);
    }
    public static void testGetResult(Collection collection){
        try {
            GetResult getResult = collection.get("test1");
            JsonObject resultThreads = getResult.contentAsObject();
            System.out.println(resultThreads);
        } catch (DocumentNotFoundException ex) {
            System.err.println("Document with the given id not found");
        } catch (CouchbaseException ex) {
            System.err.println("Something else happened: " + ex);
        }
    }
    public static void testQuery(Cluster cluster){
        try {
            final QueryResult result = cluster.query("select * from `CodeCreatedBucket` where id = \"test1\" limit 10 ",
                    queryOptions().metrics(true));

            for (JsonObject row : result.rowsAsObject()) {
                System.out.println("Found row: " + row);
            }

            System.out.println("Reported execution time: " + result.metaData().metrics().get().executionTime());
        } catch (CouchbaseException ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {

        // Connect to DB
        Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "123456");

        // The Table (Bucket) RecommendedThreads
        Bucket threadsBucket = cluster.bucket("RecommendedThreads");
        Collection threadsCollection = threadsBucket.defaultCollection();

        // The Table (Bucket) RecommendedUsers
        Bucket usersBucket = cluster.bucket("RecommendedUsers");
        Collection usersCollection = usersBucket.defaultCollection();

        // The Table (Bucket) RecommendedSubthreads
//        Bucket subthreadsBucket = cluster.bucket("RecommendedSubthreads");
//        Collection subthreadsCollection = subthreadsBucket.defaultCollection();

//        testQuery(cluster); // Must create a primary index on desired table before querying

        // create bucket
        BucketManager bucketManager = cluster.buckets();
//        bucketManager.createBucket(
//                BucketSettings.create("CodeCreatedBucket")
//                        .ramQuotaMB(100));

        Bucket codeBucket = cluster.bucket("CodeCreatedBucket");
        Collection codeBucketCollection = codeBucket.defaultCollection();
        MutationResult result = codeBucketCollection.upsert("test1", JsonObject.create().put("testField", "testValue"));
        result = codeBucketCollection.upsert("test2", JsonObject.create().put("testField", "testValue"));

        testQuery(cluster);




//        GetResult result = usersCollection.get("mark");
//        JsonObject content = result.contentAsObject();
//        content.put("modified", true).put("initial", false);
//        usersCollection.replace("mark", content, replaceOptions().cas(result.cas()));

//        try {
//            JsonArray threads = JsonArray.create().add("threadId1").add("threadId2");
//            JsonObject content = JsonObject.create().put("threads", threads);
//            MutationResult insertResult = threadsCollection.insert("user3", content);
//        } catch (DocumentExistsException ex) {
//            System.err.println("The document already exists!");
//        } catch (CouchbaseException ex) {
//            System.err.println("Something else happened: " + ex);
//        }

    }

}
