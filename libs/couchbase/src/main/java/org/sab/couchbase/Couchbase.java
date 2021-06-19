package org.sab.couchbase;

import com.couchbase.client.core.diagnostics.PingState;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.manager.bucket.BucketSettings;
import com.couchbase.client.java.manager.bucket.BucketType;
import com.couchbase.client.java.manager.bucket.EvictionPolicyType;
import org.json.JSONObject;

import java.time.Duration;

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

    public static org.json.JSONObject convertToJson(JsonObject jsonObject) {
        return new org.json.JSONObject(jsonObject.toMap());
    }

    private void connect() {
        if (cluster != null)
            disconnect();
        cluster = Cluster.connect(System.getenv("COUCHBASE_HOST"),
                System.getenv("COUCHBASE_USERNAME"),
                System.getenv("COUCHBASE_PASSWORD"));
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
        cluster.buckets().createBucket(BucketSettings.create(bucketName)
                .ramQuotaMB(ramQuotaMB)
                .bucketType(BucketType.EPHEMERAL)
                .evictionPolicy(EvictionPolicyType.NOT_RECENTLY_USED));
    }

    public void dropBucket(String bucketName) {
        cluster.buckets().dropBucket(bucketName);
    }

    public boolean bucketExists(String bucketName) {
        return cluster.buckets().getAllBuckets().containsKey(bucketName);
    }

    public void createBucketIfNotExists(String bucketName, int ramQuotaMB) {
        if (!bucketExists(bucketName))
            createBucket(bucketName, ramQuotaMB);
    }

    public MutationResult upsertDocument(String bucketName, String documentKey, JSONObject object) {
        final JsonObject couchbaseData = JsonObject.from(object.toMap());
        return upsertDocument(bucketName, documentKey, couchbaseData);
    }

    @Deprecated
    public MutationResult upsertDocument(String bucketName, String documentKey, JsonObject object) {
        return cluster.bucket(bucketName).defaultCollection().upsert(documentKey, object);
    }

    public JSONObject getDocumentJson(String bucketName, String documentKey) {
        return convertToJson(getDocument(bucketName, documentKey));
    }

    @Deprecated
    public JsonObject getDocument(String bucketName, String documentKey) {
        GetResult getResult = cluster.bucket(bucketName).defaultCollection().get(documentKey);
        return getResult.contentAsObject();
    }

    public boolean documentExists(String bucketName, String documentKey) {
        return cluster.bucket(bucketName).defaultCollection().exists(documentKey).exists();
    }

    public MutationResult replaceDocument(String bucketName, String documentKey, JSONObject object) {
        final JsonObject couchbaseData = JsonObject.from(object.toMap());
        return replaceDocument(bucketName, documentKey, couchbaseData);
    }

    @Deprecated
    public MutationResult replaceDocument(String bucketName, String documentKey, JsonObject object) {
        return cluster.bucket(bucketName).defaultCollection().replace(documentKey, object);
    }

    public MutationResult deleteDocument(String bucketName, String documentKey) {
        return cluster.bucket(bucketName).defaultCollection().remove(documentKey);
    }
}