package org.sab.recommendation;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.mapping.ArangoJack;

public class GetPopularThreadsCommand {
    private static ArangoDB arangoDB;

    public void execute() {
        try {
            arangoDB = new ArangoDB.Builder().user(System.getenv("ARANGO_USER")).password(System.getenv("ARANGO_PASSWORD")).serializer(new ArangoJack()).build();
            ArangoDatabase db = arangoDB.db(System.getenv("ARANGO_DB"));

            String query = "for thread in Threads sort thread.NumOfFollowers desc limit 20 return thread";
            ArangoCursor<BaseDocument> cursor = db.query(query, null, null, BaseDocument.class);

            Thread thread = new Thread();
            if(cursor.hasNext()) {
                cursor.forEachRemaining(document -> {
                    thread.setName(document.getKey());
                    thread.setDescription((String) document.getProperties().get("Description"));
                    thread.setCreator((String) document.getProperties().get("Creator"));
                    thread.setNumOfFollowers((Integer) document.getProperties().get("NumOfFollowers"));
                    thread.setDateCreated((String) document.getProperties().get("DateCreated"));
                    System.out.println("Top Results " + thread);
                });
            }
            else
                System.out.println("No Threads found");
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        } finally {
            arangoDB.shutdown();
        }
    }

    public static void main(String[] args) {
        GetPopularThreadsCommand c = new GetPopularThreadsCommand();
        c.execute();
    }
}
