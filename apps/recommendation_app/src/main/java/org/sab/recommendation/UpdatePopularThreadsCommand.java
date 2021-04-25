package org.sab.recommendation;

import com.arangodb.ArangoCursor;
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
import com.couchbase.client.java.json.JsonObject;

public class UpdatePopularThreadsCommand {
    private static ArangoDB arangoDB;
    private Cluster cluster;

    public void execute() {
        JsonArray threads = JsonArray.create();
        try {
            arangoDB = new ArangoDB.Builder().user(System.getenv("ARANGO_USER")).password(System.getenv("ARANGO_PASSWORD")).serializer(new ArangoJack()).build();
            ArangoDatabase db = arangoDB.db(System.getenv("ARANGO_DB"));

            String query = "" +
                    "FOR thread IN Threads\n" +
                    "    SORT thread.NumOfFollowers DESC\n" +
                    "    LIMIT 20 \n" +
                    "    RETURN thread";
            ArangoCursor<BaseDocument> cursor = db.query(query, null, null, BaseDocument.class);

            if(cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    JsonObject thread = JsonObject.create();
                    thread.put("name", document.getKey());
                    thread.put("description", (String) document.getProperties().get("Description"));
                    thread.put("creator",(String) document.getProperties().get("Creator") );
                    thread.put("numOfFollowers", (int) document.getProperties().get("NumOfFollowers"));
                    thread.put("dateCreated", (String) document.getProperties().get("DateCreated"));
                    threads.add(thread);
                });
            }
            else
                System.out.println("No results found");
        } catch(ArangoDBException e) {
            System.err.println(e.getMessage());
        } finally {
            arangoDB.shutdown();
        }

        if(threads.size() != 0) {
            try {
                cluster = Cluster.connect(System.getenv("COUCHBASE_HOST"), System.getenv("COUCHBASE_USERNAME"), System.getenv("COUCHBASE_PASSWORD"));

                Collection recommendedThreadsCollection = cluster.bucket("Listings").defaultCollection();
                recommendedThreadsCollection.upsert("popThreads", threads);
            } catch (DocumentNotFoundException ex) {
                System.err.println("Document with the given key not found");
            } catch (CouchbaseException ex) {
                ex.printStackTrace();
            } finally {
                cluster.disconnect();
            }
        }
    }

    public static void main(String[] args) {
        UpdatePopularThreadsCommand c = new UpdatePopularThreadsCommand();
        c.execute();
    }
}
