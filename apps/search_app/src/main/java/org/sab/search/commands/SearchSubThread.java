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

public class SearchSubThread extends Command {
    private Arango arango;
    private ArangoDB arangoDB;

    @Override
    public String execute(JSONObject request) {
        try {
            String searchKeywords = request.getJSONObject("body").getString("searchKeywords");
            if (searchKeywords.isBlank())
                return Responder.makeErrorResponse("searchKeywords must not be blank", 400).toString();

            arango = Arango.getInstance();
            arangoDB = arango.connect();

            String query = """
                    FOR result IN %s
                         SEARCH ANALYZER(result.%s IN TOKENS(@keywords, "text_en") OR result.%s IN TOKENS(@keywords, "text_en"), "text_en")
                         RETURN result"""
                    .formatted(SearchApp.getViewName(SearchApp.subThreadsCollectionName),
                            SearchApp.subThreadTitle,
                            SearchApp.subThreadContent);
            Map<String, Object> bindVars = Collections.singletonMap("keywords", searchKeywords);
            ArangoCursor<BaseDocument> cursor = arango.query(arangoDB, SearchApp.dbName, query, bindVars);

            JSONArray data = new JSONArray();
            cursor.forEachRemaining(document -> {
                JSONObject subThread = new JSONObject();
                subThread.put(SearchApp.subThreadId, document.getKey());
                subThread.put(SearchApp.subThreadParentThread, document.getProperties().get(SearchApp.subThreadParentThread));
                subThread.put(SearchApp.subThreadTitle, document.getProperties().get(SearchApp.subThreadTitle));
                subThread.put(SearchApp.subThreadCreator, document.getProperties().get(SearchApp.subThreadCreator));
                subThread.put(SearchApp.subThreadLikes, document.getProperties().get(SearchApp.subThreadLikes));
                subThread.put(SearchApp.subThreadDislikes, document.getProperties().get(SearchApp.subThreadDislikes));
                subThread.put(SearchApp.subThreadContent, document.getProperties().get(SearchApp.subThreadContent));
                subThread.put(SearchApp.subThreadHasImage, document.getProperties().get(SearchApp.subThreadHasImage));
                subThread.put(SearchApp.subThreadTime, document.getProperties().get(SearchApp.subThreadTime));
                data.put(subThread);
            });
            return Responder.makeDataResponse(data).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400).toString();
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500).toString();
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong.", 500).toString();
        } finally {
            if (arango != null)
                arango.disconnect(arangoDB);
        }
    }
}
