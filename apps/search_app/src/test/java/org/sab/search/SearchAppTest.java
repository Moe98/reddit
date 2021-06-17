package org.sab.search;

import com.arangodb.entity.BaseDocument;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.models.SubThread;
import org.sab.models.Thread;
import org.sab.search.commands.SearchSubThread;
import org.sab.search.commands.SearchThread;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SearchAppTest {
    static String dbName = SearchApp.DB_NAME;
    static String threadsCollectionName = SearchApp.THREADS_COLLECTION_NAME;
    static String threadName = SearchApp.THREAD_NAME;
    static String subThreadsCollectionName = SearchApp.SUB_THREADS_COLLECTION_NAME;
    static String subThreadId = SearchApp.SUB_THREAD_ID;
    static Arango arango;
    static HashMap<String, ArrayList<String>> toBeDeleted;
    static String[] threads;
    static String[] subThreadsKeys;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();

            SearchApp.dbInit();

            // Dummy Data
            toBeDeleted = new HashMap<>();
            toBeDeleted.put(threadsCollectionName, new ArrayList<>());
            toBeDeleted.put(subThreadsCollectionName, new ArrayList<>());

            threads = new String[]{"ThreadForTestSearchApp", "ThreadTestTestSearchApp"};
            String[] subThreadsTitles = new String[]{"i love scalable", "i went to eat ice cream"};
            subThreadsKeys = new String[2];
            for (String s : threads) {
                BaseDocument thread = new BaseDocument();
                thread.setKey(s);
                thread.addAttribute(Thread.getDescriptionAttributeName(), "desc");
                thread.addAttribute(Thread.getCreatorAttributeName(), "hamada");
                thread.addAttribute(Thread.getNumOfFollowersAttributeName(), 0);
                thread.addAttribute(Thread.getDateCreatedAttributeName(), Timestamp.valueOf(LocalDateTime.now()));
                arango.createDocument(dbName, threadsCollectionName, thread);
                toBeDeleted.get(threadsCollectionName).add(s);
            }
            for (int i = 0; i < 2; i++) {
                BaseDocument subThread = new BaseDocument();
                subThread.addAttribute(SubThread.getParentThreadAttributeName(), threads[0]);
                subThread.addAttribute(SubThread.getTitleAttributeName(), subThreadsTitles[i]);
                subThread.addAttribute(SubThread.getCreatorAttributeName(), "hamada");
                subThread.addAttribute(SubThread.getLikesAttributeName(), 0);
                subThread.addAttribute(SubThread.getDislikesAttributeName(), 0);
                subThread.addAttribute(SubThread.getContentAttributeName(), "content");
                subThread.addAttribute(SubThread.getHasImageAttributeName(), false);
                subThread.addAttribute(SubThread.getDateAttributeName(), Timestamp.valueOf(LocalDateTime.now()));
                BaseDocument created = arango.createDocument(dbName, subThreadsCollectionName, subThread);
                toBeDeleted.get(subThreadsCollectionName).add(created.getKey());
                subThreadsKeys[i] = created.getKey();
            }
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        try {
            toBeDeleted.forEach((key, value) -> {
                for (String _key : value) {
                    arango.deleteDocument(dbName, key, _key);
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            arango.disconnect();
        }
    }

    private static JSONObject makeRequest(String input) {
        JSONObject request = new JSONObject();
        request.put("methodType", "POST");
        request.put("body", new JSONObject().put(SearchApp.SEARCH_KEYWORDS, input));
        request.put("uriParams", new JSONObject());
        return request;
    }

    @Test
    public void SearchThread() {
        try {
            JSONObject responseJson = new JSONObject(new SearchThread().execute(makeRequest("ThreadForTestSearch")));
            assertEquals(200, responseJson.getInt("statusCode"));
            assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(threadName).equals(threads[0]));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void SearchSubThread() {
        try {
            JSONObject responseJson = new JSONObject(new SearchSubThread().execute(makeRequest("ice cream")));
            assertEquals(200, responseJson.getInt("statusCode"));
            assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(subThreadId).equals(subThreadsKeys[1]));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }
}