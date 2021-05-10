package org.sab.couchbase;

import com.couchbase.client.core.diagnostics.PingState;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.manager.query.DropPrimaryQueryIndexOptions;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;

import java.time.Duration;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;

@SuppressWarnings("unused")
public class Couchbase {
    final private static Couchbase instance = new Couchbase();

    private Cluster cluster;

    private Couchbase() {
        connect();
    }

    public static Couchbase getInstance() {
        return instance;
    }

    private void connect() {
        if (cluster != null)
            disconnect();
        cluster = Cluster.connect(System.getenv("COUCHBASE_HOST"), System.getenv("COUCHBASE_USERNAME"), System.getenv("COUCHBASE_PASSWORD"));
        cluster.waitUntilReady(Duration.ofSeconds(3));
    }

    public boolean isConnected() {
        if (cluster == null)
            return false;
        return cluster.ping().endpoints().values().stream().anyMatch(a -> a.stream().anyMatch(b -> b.state() == PingState.OK));
    }

    public void connectIfNotConnected() {
        if (!isConnected())
            connect();
    }

    public void disconnect() {
        if (cluster != null) {
            cluster.disconnect();
            cluster = null;
        }
    }

    public void createBucket(String bucketName, int ramQuotaMB) {
        cluster.buckets().createBucket(BucketSettings.create(bucketName).ramQuotaMB(ramQuotaMB));
        cluster.queryIndexes().createPrimaryIndex(bucketName);
    }

    public void dropBucket(String bucketName) {
        cluster.queryIndexes().dropPrimaryIndex(bucketName, DropPrimaryQueryIndexOptions.dropPrimaryQueryIndexOptions().ignoreIfNotExists(true));
        cluster.buckets().dropBucket(bucketName);
    }

    public boolean bucketExists(String bucketName) {
        return cluster.buckets().getAllBuckets().containsKey(bucketName);
    }

    public void createBucketIfNotExists(String bucketName, int ramQuotaMB) {
        if (!bucketExists(bucketName))
            createBucket(bucketName, ramQuotaMB);
        else
            cluster.queryIndexes().createPrimaryIndex(bucketName, CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().ignoreIfExists(true));
    }

    public MutationResult upsertDocument(String bucketName, String documentKey, JsonObject object) {
        return cluster.bucket(bucketName).defaultCollection().upsert(documentKey, object);
    }

    public JsonObject getDocument(String bucketName, String documentKey) {
        GetResult getResult = cluster.bucket(bucketName).defaultCollection().get(documentKey);
        return getResult.contentAsObject();
    }

    public boolean documentExists(String bucketName, String documentKey) {
        return cluster.bucket(bucketName).defaultCollection().exists(documentKey).exists();
    }

    public MutationResult replaceDocument(String bucketName, String documentKey, JsonObject object) {
        return cluster.bucket(bucketName).defaultCollection().replace(documentKey, object);
    }

    public MutationResult deleteDocument(String bucketName, String documentKey) {
        return cluster.bucket(bucketName).defaultCollection().remove(documentKey);
    }

    public QueryResult query(String queryText) {
        return query(queryText, false);
    }

    public QueryResult query(String queryText, boolean consistent) {
        return cluster.query(queryText, queryOptions().scanConsistency(consistent ? QueryScanConsistency.REQUEST_PLUS : QueryScanConsistency.NOT_BOUNDED));
    }
}