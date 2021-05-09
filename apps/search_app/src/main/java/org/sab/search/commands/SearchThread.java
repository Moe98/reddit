package org.sab.search.commands;

import com.arangodb.ArangoCursor;
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

    @Override
    public String execute(JSONObject request) {
        try {
            String searchKeyword = request.getJSONObject("body").getString("searchKeyword");
            if (searchKeyword.isBlank())
                return Responder.makeErrorResponse("searchKeyword must not be blank", 400).toString();

            Arango arango = Arango.getInstance();
            arango.connectIfNotConnected();

            // Search Threads using an English text analyzer to search for the keyword appearing in a prefix of Threads'
            // names.
            String query = """
                    FOR result IN %s
                         SEARCH ANALYZER(STARTS_WITH(result.%s, LOWER(LTRIM(@keyword))) OR PHRASE(result.%s, @keyword), "text_en")
                         RETURN result"""
                    .formatted(SearchApp.getViewName(SearchApp.THREADS_COLLECTION_NAME),
                            SearchApp.THREAD_NAME,
                            SearchApp.THREAD_NAME);
            Map<String, Object> bindVars = Collections.singletonMap("keyword", searchKeyword);
            ArangoCursor<BaseDocument> cursor = arango.query(SearchApp.DB_NAME, query, bindVars);

            JSONArray data = new JSONArray();
            cursor.forEachRemaining(document -> {
                JSONObject thread = new JSONObject();
                thread.put(SearchApp.THREAD_NAME, document.getKey());
                thread.put(SearchApp.THREAD_DESCRIPTION, document.getProperties().get(SearchApp.THREAD_DESCRIPTION));
                thread.put(SearchApp.THREAD_CREATOR, document.getProperties().get(SearchApp.THREAD_CREATOR));
                thread.put(SearchApp.THREAD_FOLLOWERS, document.getProperties().get(SearchApp.THREAD_FOLLOWERS));
                thread.put(SearchApp.THREAD_DATE, document.getProperties().get(SearchApp.THREAD_DATE));
                data.put(thread);
            });
            return Responder.makeDataResponse(data).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400).toString();
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong.", 500).toString();
        }
    }
}
