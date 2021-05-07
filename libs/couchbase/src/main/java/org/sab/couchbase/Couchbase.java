package org.sab.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;

@SuppressWarnings("unused")
public class Couchbase {
    private static Couchbase instance = null;

    private Couchbase() {
    }

    public static Couchbase getInstance() {
        if (instance == null)
            instance = new Couchbase();
        return instance;
    }

    public Cluster connect() {
        return Cluster.connect(System.getenv("COUCHBASE_HOST"), System.getenv("COUCHBASE_USERNAME"), System.getenv("COUCHBASE_PASSWORD"));
    }

    public void disconnect(Cluster cluster) {
        cluster.disconnect();
    }

    public void createBucket(Cluster cluster, String bucketName, int ramQuotaMB) {
        cluster.buckets().createBucket(BucketSettings.create(bucketName).ramQuotaMB(ramQuotaMB));
        cluster.query("CREATE PRIMARY INDEX on `default` : `" + bucketName + "`;");
    }

    public void dropBucket(Cluster cluster, String bucketName) {
        cluster.query("DROP PRIMARY INDEX on `default` : `" + bucketName + "`;");
        cluster.buckets().dropBucket(bucketName);
    }

    public boolean bucketExists(Cluster cluster, String bucketName) {
        return cluster.buckets().getAllBuckets().containsKey(bucketName);
    }

    public void createBucketIfNotExists(Cluster cluster, String bucketName, int ramQuotaMB) {
        if (!bucketExists(cluster, bucketName))
            createBucket(cluster, bucketName, ramQuotaMB);
    }

    public MutationResult upsertDocument(Cluster cluster, String bucketName, String documentKey, JsonObject object) {
        return cluster.bucket(bucketName).defaultCollection().upsert(documentKey, object);
    }

    public JsonObject getDocument(Cluster cluster, String bucketName, String documentKey) {
        GetResult getResult = cluster.bucket(bucketName).defaultCollection().get(documentKey);
        return getResult.contentAsObject();
    }

    public boolean documentExists(Cluster cluster, String bucketName, String documentKey) {
        return cluster.bucket(bucketName).defaultCollection().exists(documentKey).exists();
    }

    public MutationResult replaceDocument(Cluster cluster, String bucketName, String documentKey, JsonObject object) {
        return cluster.bucket(bucketName).defaultCollection().replace(documentKey, object);
    }

    public MutationResult deleteDocument(Cluster cluster, String bucketName, String documentKey) {
        return cluster.bucket(bucketName).defaultCollection().remove(documentKey);
    }

    public QueryResult query(Cluster cluster, String queryText) {
        return query(cluster, queryText, false);
    }

    public QueryResult query(Cluster cluster, String queryText, boolean consistent) {
        return cluster.query(queryText, queryOptions().scanConsistency(consistent ? QueryScanConsistency.REQUEST_PLUS : QueryScanConsistency.NOT_BOUNDED));
    }
}
