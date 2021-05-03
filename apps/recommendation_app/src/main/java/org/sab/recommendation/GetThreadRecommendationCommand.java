package org.sab.recommendation;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import org.sab.couchbase.Couchbase;
import org.sab.models.Thread;

import java.util.HashMap;

public class GetThreadRecommendationCommand {
    private HashMap<String, String> parameters;
    private Couchbase couchbase;
    private Cluster cluster;

    public void execute() {
        try {
            couchbase = Couchbase.getInstance();
            cluster = couchbase.connect();

            JsonObject result = couchbase.getDocument(cluster, "RecommendedThreads", parameters.get("username"));
            JsonArray resultThreads = result.getArray("listOfThreads");


            Thread thread = new Thread();
            for(int i = 0; i<resultThreads.size(); i++) {
                JsonObject o = resultThreads.getObject(i);
                thread.setName(o.getString("name"));
                thread.setDescription(o.getString("description"));
                thread.setCreatorId(o.getString("creator"));
                thread.setNumOfFollowers(o.getLong("numOfFollowers"));
                thread.setDateCreated((String) o.get("dateCreated"));
                System.out.println("Recommendation Results " + thread);
            }
        } catch (DocumentNotFoundException ex) {
            System.err.println("Document with the given username not found");
        } finally {
            couchbase.disconnect(cluster);
        }
    }

    public static void main(String[] args) {
        GetThreadRecommendationCommand c = new GetThreadRecommendationCommand();
        c.parameters = new HashMap<>();
        c.parameters.put("username", "hamada");
        c.execute();
    }
}
