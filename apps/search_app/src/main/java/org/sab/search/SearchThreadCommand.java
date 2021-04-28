package org.sab.search;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.models.Thread;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SearchThreadCommand {
    protected HashMap<String, String> parameters;
    private Arango arango;
    private ArangoDB arangoDB;

    public void execute() {
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            if(!arangoDB.db(System.getenv("ARANGO_DB")).view("ThreadsView").exists()){
                arango.createView(arangoDB, System.getenv("ARANGO_DB"), "ThreadsView", "Threads", new String[] {"Description"});
            }

            String query = "" +
                    "FOR result IN ThreadsView\n" +
                    "    SEARCH PHRASE(result.Description, @words, \"text_en\")\n" +
                    "    RETURN result";
            Map<String, Object> bindVars = Collections.singletonMap("words", parameters.get("searchText"));
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, System.getenv("ARANGO_DB"), query, bindVars);

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
                System.out.println("No results found");
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        } finally {
            arango.disconnect(arangoDB);
        }
    }

    public static void main(String[] args) {
        SearchThreadCommand c = new SearchThreadCommand();
        c.parameters = new HashMap<>();
        c.parameters.put("searchText", "fail");
        c.execute();
    }
}