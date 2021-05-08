package org.sab.couchbase;

import com.couchbase.client.core.diagnostics.ClusterState;
import com.couchbase.client.core.diagnostics.PingState;
import com.couchbase.client.core.service.ServiceType;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.diagnostics.PingOptions;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryScanConsistency;

import java.time.Duration;
import java.util.EnumSet;

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

    public void connect() {
        if (cluster != null)
            disconnect();
        cluster = Cluster.connect(System.getenv("COUCHBASE_HOST"), System.getenv("COUCHBASE_USERNAME"), System.getenv("COUCHBASE_PASSWORD"));
    }

    public boolean isConnected() {
        if (cluster == null)
            return false;
        cluster.waitUntilReady(Duration.ofSeconds(3));
        return cluster.ping().endpoints().values().stream().anyMatch(a -> a.stream().anyMatch(b -> b.state() == PingState.OK));
    }

    public void disconnect() {
        cluster.disconnect();
        cluster = null;
    }

    public void createBucket(String bucketName, int ramQuotaMB) {
        cluster.buckets().createBucket(BucketSettings.create(bucketName).ramQuotaMB(ramQuotaMB));
        cluster.query("CREATE PRIMARY INDEX on `default` : `" + bucketName + "`;");
    }

    public void dropBucket(String bucketName) {
        cluster.query("DROP PRIMARY INDEX on `default` : `" + bucketName + "`;");
        cluster.buckets().dropBucket(bucketName);
    }

    public boolean bucketExists(String bucketName) {
        return cluster.buckets().getAllBuckets().containsKey(bucketName);
    }

    public void createBucketIfNotExists(String bucketName, int ramQuotaMB) {
        if (!bucketExists(bucketName))
            createBucket(bucketName, ramQuotaMB);
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
