package org.sab.recommendation;


import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.kv.MutationResult;

public class App {
    public static void main(String[] args) {
        Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "scaleabull");
        Bucket bucket = cluster.bucket("RecommendedThreads");
        Collection collection = bucket.defaultCollection();

        JsonArray threads = JsonArray.create().add("id1").add("id2");
        MutationResult upsertResult = collection.upsert("hamada", threads);
    }

}
