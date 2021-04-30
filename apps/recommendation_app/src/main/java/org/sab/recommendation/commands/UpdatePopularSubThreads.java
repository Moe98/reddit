package org.sab.recommendation.commands;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JacksonTransformers;
import com.couchbase.client.java.json.JsonObject;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.models.SubThread;
import org.sab.service.Command;

public class UpdatePopularSubThreads extends Command {
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
                    FOR subThread IN SubThreads
                        SORT subThread.Likes DESC
                        LIMIT 100
                        RETURN subThread""";
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, null);

            ArrayNode data = nf.arrayNode();
            if(cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    SubThread subThread = new SubThread();
                    subThread.set_key(document.getKey());
                    subThread.setParentThread((String) document.getProperties().get("ParentThread"));
                    subThread.setTitle((String) document.getProperties().get("Title"));
                    subThread.setCreator((String) document.getProperties().get("Creator"));
                    subThread.setLikes((Integer) document.getProperties().get("Likes"));
                    subThread.setDislikes((Integer) document.getProperties().get("Dislikes"));
                    subThread.setContent((String) document.getProperties().get("Content"));
                    subThread.setHasImage((Boolean) document.getProperties().get("HasImage"));
                    subThread.setTime((String) document.getProperties().get("Time"));
                    data.addPOJO(subThread);
                });
                response.set("data", data);
            }
            else {
                response.set("msg", nf.textNode("No Result"));
                response.set("data", nf.arrayNode());
            }
        } catch(Exception e) {
            response.set("msg", nf.textNode(e.getMessage()));
            response.set("data", nf.arrayNode());
            response.set("statusCode", nf.numberNode(500));
        } finally {
            arango.disconnect(arangoDB);
        }

        if(response.get("data").size() != 0) {
            try {
                couchbase = Couchbase.getInstance();
                cluster = couchbase.connect();

                if(!cluster.buckets().getAllBuckets().containsKey("Listings")){
                    couchbase.createBucket(cluster, "Listings", 100);
                }

                JsonObject object = JsonObject.create().put("listOfSubThreads", JacksonTransformers.stringToJsonArray(response.get("data").toString()));
                couchbase.upsertDocument(cluster, "Listings", "popSubThreads", object);
                response.set("msg", nf.textNode("Popular SubThreads Updated Successfully!"));
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
