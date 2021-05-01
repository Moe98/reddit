package org.sab.recommendation.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.json.JacksonTransformers;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.Thread;
import org.sab.service.Command;

public class UpdatePopularThreads extends Command {
    private Arango arango;
    private ArangoDB arangoDB;
    private Couchbase couchbase;
    private Cluster cluster;

    @Override
    public String execute(JSONObject request) {
        JsonNodeFactory nf = JsonNodeFactory.instance;
        ObjectNode response = nf.objectNode();
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            String query = """
                    FOR thread IN Threads
                        SORT thread.NumOfFollowers DESC
                        LIMIT 100
                        RETURN thread""";
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, null);

            ArrayNode data = nf.arrayNode();
            if (cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    Thread thread = new Thread();
                    thread.setName(document.getKey());
                    thread.setDescription((String) document.getProperties().get("Description"));
                    thread.setCreator((String) document.getProperties().get("Creator"));
                    thread.setNumOfFollowers((Integer) document.getProperties().get("NumOfFollowers"));
                    thread.setDateCreated((String) document.getProperties().get("DateCreated"));
                    data.addPOJO(thread);
                });
                response.set("data", data);
            } else {
                response.set("msg", nf.textNode("No Result"));
                response.set("data", nf.arrayNode());
            }
        } catch (Exception e) {
            response.set("msg", nf.textNode(e.getMessage()));
            response.set("data", nf.arrayNode());
            response.set("statusCode", nf.numberNode(500));
        } finally {
            arango.disconnect(arangoDB);
        }

        if (response.get("data").size() != 0) {
            try {
                couchbase = Couchbase.getInstance();
                cluster = couchbase.connect();

                if (!couchbase.bucketExists(cluster, "Listings")) {
                    couchbase.createBucket(cluster, "Listings", 100);
                }

                JsonObject object = JsonObject.create().put("listOfThreads", JacksonTransformers.stringToJsonArray(response.get("data").toString()));
                couchbase.upsertDocument(cluster, "Listings", "popThreads", object);
                response.set("msg", nf.textNode("Popular Threads Updated Successfully!"));
                response.set("statusCode", nf.numberNode(200));
            } catch (Exception e) {
                response.set("msg", nf.textNode(e.getMessage()));
                response.set("data", nf.arrayNode());
                response.set("statusCode", nf.numberNode(500));
            } finally {
                couchbase.disconnect(cluster);
            }
        }
        return response.toString();
    }
}
