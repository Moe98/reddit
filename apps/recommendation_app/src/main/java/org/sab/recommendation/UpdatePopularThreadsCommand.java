package org.sab.recommendation;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;

public class UpdatePopularThreadsCommand {
    private Arango arango;
    private ArangoDB arangoDB;
    private Couchbase couchbase;
    private Cluster cluster;

    public void execute() {
        JsonArray threads = JsonArray.create();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            String query = "" +
                    "FOR thread IN Threads\n" +
                    "    SORT thread.NumOfFollowers DESC\n" +
                    "    LIMIT 20 \n" +
                    "    RETURN thread";
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, null);

            if(cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    JsonObject thread = JsonObject.create();
                    thread.put("name", document.getKey());
                    thread.put("description", (String) document.getProperties().get("Description"));
                    thread.put("creator",(String) document.getProperties().get("Creator") );
                    thread.put("numOfFollowers", (long) document.getProperties().get("NumOfFollowers"));
                    thread.put("dateCreated", (String) document.getProperties().get("DateCreated"));
                    threads.add(thread);
                });
            }
            else
                System.out.println("No results found");
        } catch(ArangoDBException e) {
            System.err.println(e.getMessage());
        } finally {
            arango.disconnect(arangoDB);
        }

        if(threads.size() != 0) {
            try {
                couchbase = Couchbase.getInstance();
                cluster = couchbase.connect();

                if(!cluster.buckets().getAllBuckets().containsKey("Listings")){
                    couchbase.createBucket(cluster, "Listings", 100);
                }

                JsonObject object = JsonObject.create().put("listOfThreads", threads);
                couchbase.upsertDocument(cluster, "Listings", "popThreads", object);
            } catch (DocumentNotFoundException ex) {
                System.err.println("Document with the given key not found");
            } catch (CouchbaseException ex) {
                ex.printStackTrace();
            } finally {
                couchbase.disconnect(cluster);
            }
        }
    }

    public static void main(String[] args) {
        UpdatePopularThreadsCommand c = new UpdatePopularThreadsCommand();
        c.execute();
    }
}
