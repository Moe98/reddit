package org.sab.recommendation;

import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;

import java.util.HashMap;

public class GetThreadRecommendationCommand {
    private HashMap<String, String> parameters;
    private Cluster cluster;

    public void execute() {
        try {
            cluster = Cluster.connect(System.getenv("COUCHBASE_HOST"), System.getenv("COUCHBASE_USERNAME"), System.getenv("COUCHBASE_PASSWORD"));

            Collection recommendedThreadsCollection = cluster.bucket("RecommendedThreads").defaultCollection();
            GetResult getResult = recommendedThreadsCollection.get(parameters.get("username"));
            JsonArray resultThreads = getResult.contentAsArray();

            Thread thread = new Thread();
            for(int i = 0; i<resultThreads.size(); i++) {
                JsonObject o = resultThreads.getObject(i);
                thread.setName(o.getString("_key"));
                thread.setDescription(o.getString("Description"));
                thread.setCreator(o.getString("Creator"));
                thread.setNumOfFollowers(o.getInt("NumOfFollowers"));
                thread.setDateCreated((String) o.get("DateCreated"));
                System.out.println("Recommendation Results " + thread);
            }
        } catch (DocumentNotFoundException ex) {
            System.err.println("Document with the given username not found");
        } catch (CouchbaseException ex) {
            ex.printStackTrace();
        } finally {
            cluster.disconnect();
        }
    }

    public static void main(String[] args) {
        GetThreadRecommendationCommand c = new GetThreadRecommendationCommand();
        c.parameters = new HashMap<>();
        c.parameters.put("username", "hamada");
        c.execute();
    }
}
