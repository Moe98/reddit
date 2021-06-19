package org.sab.search.commands;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.search.SearchApp;
import org.sab.service.Responder;
import org.sab.service.validation.CommandWithVerification;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchSubThread extends CommandWithVerification {

    @Override
    public String execute() {
        try {
            String searchKeywords = body.getString(SearchApp.SEARCH_KEYWORDS);
            if (searchKeywords.isBlank())
                return Responder.makeErrorResponse("searchKeywords must not be blank", 400);

            Arango arango = Arango.getInstance();

            // Search SubThread by using an English text analyzer to search for keywords appearing in SubThreads' Titles
            // & Contents.
            String query = """
                    FOR result IN %s
                         SEARCH ANALYZER(result.%s IN TOKENS(@keywords, "text_en") OR result.%s IN TOKENS(@keywords, "text_en"), "text_en")
                         RETURN result"""
                    .formatted(SearchApp.getViewName(SearchApp.SUB_THREADS_COLLECTION_NAME),
                            SearchApp.SUB_THREAD_TITLE,
                            SearchApp.SUB_THREAD_CONTENT);
            Map<String, Object> bindVars = Collections.singletonMap("keywords", searchKeywords);
            ArangoCursor<BaseDocument> cursor = arango.query(SearchApp.DB_NAME, query, bindVars);

            JSONArray data = new JSONArray();
            cursor.forEachRemaining(document -> {
                JSONObject subThread = new JSONObject();
                subThread.put(SearchApp.SUB_THREAD_ID, document.getKey());
                subThread.put(SearchApp.SUB_THREAD_PARENT_THREAD, document.getProperties().get(SearchApp.SUB_THREAD_PARENT_THREAD));
                subThread.put(SearchApp.SUB_THREAD_TITLE, document.getProperties().get(SearchApp.SUB_THREAD_TITLE));
                subThread.put(SearchApp.SUB_THREAD_CREATOR, document.getProperties().get(SearchApp.SUB_THREAD_CREATOR));
                subThread.put(SearchApp.SUB_THREAD_LIKES, document.getProperties().get(SearchApp.SUB_THREAD_LIKES));
                subThread.put(SearchApp.SUB_THREAD_DISLIKES, document.getProperties().get(SearchApp.SUB_THREAD_DISLIKES));
                subThread.put(SearchApp.SUB_THREAD_CONTENT, document.getProperties().get(SearchApp.SUB_THREAD_CONTENT));
                subThread.put(SearchApp.SUB_THREAD_HAS_IMAGE, document.getProperties().get(SearchApp.SUB_THREAD_HAS_IMAGE));
                subThread.put(SearchApp.SUB_THREAD_DATE, document.getProperties().get(SearchApp.SUB_THREAD_DATE));
                data.put(subThread);
            });
            return Responder.makeDataResponse(data).toString();
        } catch (JSONException e) {
            return Responder.makeErrorResponse("Bad Request: " + e.getMessage(), 400);
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB error: " + e.getMessage(), 500);
        } catch (Exception e) {
            return Responder.makeErrorResponse("Something went wrong.", 500);
        }
    }

    @Override
    protected Schema getSchema() {
        Attribute keywords = new Attribute(SearchApp.SEARCH_KEYWORDS, DataType.STRING, true);
        return new Schema(List.of(keywords));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }
}
