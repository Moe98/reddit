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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UpdateThreadRecommendationCommand {
    public HashMap<String, String> parameters;
    private static ArangoDB arangoDB;
    private Cluster cluster;

    public void execute() {
        try {
            arangoDB = new ArangoDB.Builder().user(System.getenv("ARANGO_USER")).password(System.getenv("ARANGO_PASSWORD")).serializer(new ArangoJack()).build();
            ArangoDatabase db = arangoDB.db(System.getenv("ARANGO_DB"));

            parameters.put("username", "Users/" + parameters.get("username"));
            String query = "LET followed = (\n" +
                    "    FOR thread, edge IN 1..1 OUTBOUND @username UserFollowThread\n" +
                    "        SORT edge.date DESC\n" +
                    "        RETURN thread\n" +
                    ")\n" +
                    "LET recommendations = (\n" +
                    "    FOR thread in followed\n" +
                    "        LIMIT 5\n" +
                    "        LET subRecommendation = (FOR result IN ThreadsView\n" +
                    "            SEARCH ANALYZER(result.Description IN TOKENS(thread.Description, 'text_en'), 'text_en')\n" +
                    "            SORT BM25(result) DESC\n" +
                    "            LIMIT 5\n" +
                    "            RETURN result\n" +
                    "        )\n" +
                    "        RETURN subRecommendation\n" +
                    ")\n" +
                    "LET uniqueRecommendations = (\n" +
                    "    FOR thread in FLATTEN(recommendations)\n" +
                    "        FILTER thread not in followed\n" +
                    "        RETURN DISTINCT thread\n" +
                    ")\n" +
                    "LET mostPopular = (\n" +
                    "    FOR thread IN Threads \n" +
                    "        SORT thread.NumOfFollowers DESC\n" +
                    "        LIMIT 100\n" +
                    "        RETURN thread\n" +
                    ")\n" +
                    "LET fill = (\n" +
                    "    FOR thread in mostPopular\n" +
                    "        FILTER thread not in followed AND thread not in uniqueRecommendations\n" +
                    "        SORT RAND()\n" +
                    "        LIMIT 25\n" +
                    "        RETURN thread\n" +
                    ")\n" +
                    "FOR thread IN SLICE(APPEND(uniqueRecommendations, fill), 0, 25)\n" +
                    "    RETURN thread";
            Map<String, Object> bindVars = Collections.singletonMap("username", parameters.get("username"));
            ArangoCursor<BaseDocument> cursor = db.query(query, bindVars, null, BaseDocument.class);

            JsonArray threads = JsonArray.create();
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

                try {
                    cluster = Cluster.connect(System.getenv("COUCHBASE_HOST"), System.getenv("COUCHBASE_USERNAME"), System.getenv("COUCHBASE_PASSWORD"));

                    Collection recommendedThreadsCollection = cluster.bucket("RecommendedThreads").defaultCollection();
                    recommendedThreadsCollection.upsert(parameters.get("username").split("/")[1], threads);
                } catch (DocumentNotFoundException ex) {
                    System.err.println("Document with the given username not found");
                } catch (CouchbaseException ex) {
                    ex.printStackTrace();
                } finally {
                    cluster.disconnect();
                }
            }
            else
                System.out.println("No results found");

        } catch(ArangoDBException e) {
            System.err.println(e.getMessage());
        } finally {
            arangoDB.shutdown();
        }
    }

    public static void main(String[] args) {
        UpdateThreadRecommendationCommand c = new UpdateThreadRecommendationCommand();
        c.parameters = new HashMap<>();
        c.parameters.put("username", "hamada");
        c.execute();
    }
}
