package org.sab.recommendation;

import com.arangodb.ArangoDB;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;

public class GetPopularThreadsCommand {
    private static ArangoDB arangoDB;
    private Cluster cluster;

    public void execute() {
        try {
            cluster = Cluster.connect(System.getenv("COUCHBASE_HOST"), System.getenv("COUCHBASE_USERNAME"), System.getenv("COUCHBASE_PASSWORD"));

            Collection recommendedThreadsCollection = cluster.bucket("Listings").defaultCollection();
            GetResult getResult = recommendedThreadsCollection.get("popThreads");
            JsonArray resultThreads = getResult.contentAsArray();

            Thread thread = new Thread();
            for(int i = 0; i<resultThreads.size(); i++) {
                JsonObject o = resultThreads.getObject(i);
                thread.setName(o.getString("name"));
                thread.setDescription(o.getString("description"));
                thread.setCreator(o.getString("creator"));
                thread.setNumOfFollowers(o.getInt("numOfFollowers"));
                thread.setDateCreated((String) o.get("dateCreated"));
                System.out.println("Recommendation Results " + thread);
            }
        } catch (DocumentNotFoundException ex) {
            System.err.println("Document with the given key not found");
        } catch (CouchbaseException ex) {
            ex.printStackTrace();
        } finally {
            cluster.disconnect();
        }
    }

    public static void main(String[] args) {
        GetPopularThreadsCommand c = new GetPopularThreadsCommand();
        c.execute();
    }
}
