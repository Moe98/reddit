package org.sab.recommendation;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.mapping.ArangoJack;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonArray;

import java.util.HashMap;

public class UpdateThreadRecommendationCommand {
    private HashMap<String, String> parameters;
    private static ArangoDB arangoDB;
    private Cluster cluster;

    public void execute() {
        // Step 1 - Compute Recommended Threads
        try {
            arangoDB = new ArangoDB.Builder().user(System.getenv("ARANGO_USER")).password(System.getenv("ARANGO_PASSWORD")).serializer(new ArangoJack()).build();
            ArangoDatabase db = arangoDB.db(System.getenv("ARANGO_DB"));

        } catch(ArangoDBException e) {
            System.err.println(e.getMessage());
        } finally {
            arangoDB.shutdown();
        }

        // Step 2 - Add resulted threads to Couchbase
        try {
            cluster = Cluster.connect(System.getenv("COUCHBASE_HOST"), System.getenv("COUCHBASE_USERNAME"), System.getenv("COUCHBASE_PASSWORD"));

//            Collection recommendedThreadsCollection = cluster.bucket("RecommendedThreads").defaultCollection();
//            JsonArray threads = JsonArray.create().add("thread1").add("thread2");
//            recommendedThreadsCollection.upsert(parameters.get("username"), threads);
        } catch (DocumentNotFoundException ex) {
            System.err.println("Document with the given username not found");
        } catch (CouchbaseException ex) {
            ex.printStackTrace();
        } finally {
            cluster.disconnect();
        }
    }

    public static void main(String[] args) {
        UpdateThreadRecommendationCommand c = new UpdateThreadRecommendationCommand();
        c.parameters = new HashMap<>();
        c.parameters.put("username", "hamada");
        c.execute();
    }
}
