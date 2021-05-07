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

import java.util.HashMap;
import java.util.Map;

public class SearchThread extends Command {
    private Arango arango;
    private ArangoDB arangoDB;

    @Override
    public String execute(JSONObject request) {
        try {
            String searchKeywords = request.getJSONObject("body").getString("searchKeyword");
            if (searchKeywords == null)
                return Responder.makeErrorResponse("searchKeyword must not be null", 400).toString();
            if (searchKeywords.isBlank())
                return Responder.makeErrorResponse("searchKeyword must not be blank", 400).toString();

            arango = Arango.getInstance();
            arangoDB = arango.connect();

            String query = """
                    FOR result IN @viewName
                         SEARCH ANALYZER(STARTS_WITH(result.@nameAttribute, LOWER(LTRIM(@words))) OR PHRASE(result.@nameAttribute, @words), "text_en")
                         RETURN result""";
            Map<String, Object> bindVars = new HashMap<>();
            bindVars.put("words", searchKeywords);
            bindVars.put("viewName", SearchApp.getViewName(SearchApp.threadsCollectionName));
            bindVars.put("nameAttribute", SearchApp.threadName);
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
            return Responder.makeErrorResponse("Request doesn't have a body.", 400).toString();
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong.", 500).toString();
        } finally {
            arango.disconnect(arangoDB);
        }
    }
}
