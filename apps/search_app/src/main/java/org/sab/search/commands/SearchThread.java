package org.sab.search.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.search.SearchApp;
import org.sab.service.Command;
import org.sab.service.Responder;

import java.util.Collections;
import java.util.Map;

public class SearchThread extends Command {
    private Arango arango;
    private ArangoDB arangoDB;

    @Override
    public String execute(JSONObject request) {
        try {
            String searchKeyword = request.getJSONObject("body").getString("searchKeyword");
            if (searchKeyword.isBlank())
                return Responder.makeErrorResponse("searchKeyword must not be blank", 400).toString();

            arango = Arango.getInstance();
            arangoDB = arango.connect();

            String query = """
                    FOR result IN %s
                         SEARCH ANALYZER(STARTS_WITH(result.%s, LOWER(LTRIM(@keyword))) OR PHRASE(result.%s, @keyword), "text_en")
                         RETURN result""".formatted(SearchApp.getViewName(SearchApp.threadsCollectionName), SearchApp.threadName, SearchApp.threadName);
            Map<String, Object> bindVars = Collections.singletonMap("keyword", searchKeyword);
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, SearchApp.dbName, query, bindVars);

            JSONArray data = new JSONArray();
            cursor.forEachRemaining(document -> {
                JSONObject thread = new JSONObject();
                thread.put(SearchApp.threadName, document.getKey());
                thread.put(SearchApp.threadDescription, document.getProperties().get(SearchApp.threadDescription));
                thread.put(SearchApp.threadCreator, document.getProperties().get(SearchApp.threadCreator));
                thread.put(SearchApp.threadFollowers, document.getProperties().get(SearchApp.threadFollowers));
                thread.put(SearchApp.threadDate, document.getProperties().get(SearchApp.threadDate));
                data.put(thread);
            });
            return Responder.makeDataResponse(data).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400).toString();
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong.", 500).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
    }
}
