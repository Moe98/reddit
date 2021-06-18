package org.sab.search;

import com.arangodb.entity.BaseDocument;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.models.CollectionNames;
import org.sab.models.SubThreadAttributes;
import org.sab.models.ThreadAttributes;
import org.sab.search.commands.SearchSubThread;
import org.sab.search.commands.SearchThread;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SearchAppTest {
    final public static String THREADS_COLLECTION_NAME = CollectionNames.THREAD.get();
    final public static String THREAD_NAME = ThreadAttributes.THREAD_NAME.getDb();
    final public static String THREAD_DESCRIPTION = ThreadAttributes.DESCRIPTION.getDb();
    final public static String THREAD_CREATOR = ThreadAttributes.CREATOR_ID.getDb();
    final public static String THREAD_FOLLOWERS = ThreadAttributes.NUM_OF_FOLLOWERS.getDb();
    final public static String THREAD_DATE = ThreadAttributes.DATE_CREATED.getDb();
    final public static String SUB_THREADS_COLLECTION_NAME = CollectionNames.SUBTHREAD.get();
    final public static String SUB_THREAD_ID = SubThreadAttributes.SUBTHREAD_ID.getDb();
    final public static String SUB_THREAD_PARENT_THREAD = SubThreadAttributes.PARENT_THREAD_ID.getDb();
    final public static String SUB_THREAD_TITLE = SubThreadAttributes.TITLE.getDb();
    final public static String SUB_THREAD_CREATOR = SubThreadAttributes.CREATOR_ID.getDb();
    final public static String SUB_THREAD_LIKES = SubThreadAttributes.LIKES.getDb();
    final public static String SUB_THREAD_DISLIKES = SubThreadAttributes.DISLIKES.getDb();
    final public static String SUB_THREAD_CONTENT = SubThreadAttributes.CONTENT.getDb();
    final public static String SUB_THREAD_HAS_IMAGE = SubThreadAttributes.HAS_IMAGE.getDb();
    final public static String SUB_THREAD_DATE = SubThreadAttributes.DATE_CREATED.getDb();
    static String dbName = SearchApp.DB_NAME;
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
            toBeDeleted.put(THREADS_COLLECTION_NAME, new ArrayList<>());
            toBeDeleted.put(SUB_THREADS_COLLECTION_NAME, new ArrayList<>());

            threads = new String[]{"ThreadForTestSearchApp", "ThreadTestTestSearchApp"};
            String[] subThreadsTitles = new String[]{"i love scalable", "i went to eat ice cream"};
            subThreadsKeys = new String[2];
            for (String s : threads) {
                BaseDocument thread = new BaseDocument();
                thread.setKey(s);
                thread.addAttribute(THREAD_DESCRIPTION, "desc");
                thread.addAttribute(THREAD_CREATOR, "hamada");
                thread.addAttribute(THREAD_FOLLOWERS, 0);
                thread.addAttribute(THREAD_DATE, Timestamp.valueOf(LocalDateTime.now()));
                arango.createDocument(dbName, THREADS_COLLECTION_NAME, thread);
                toBeDeleted.get(THREADS_COLLECTION_NAME).add(s);
            }
            for (int i = 0; i < 2; i++) {
                BaseDocument subThread = new BaseDocument();
                subThread.addAttribute(SUB_THREAD_PARENT_THREAD, threads[0]);
                subThread.addAttribute(SUB_THREAD_TITLE, subThreadsTitles[i]);
                subThread.addAttribute(SUB_THREAD_CREATOR, "hamada");
                subThread.addAttribute(SUB_THREAD_LIKES, 0);
                subThread.addAttribute(SUB_THREAD_DISLIKES, 0);
                subThread.addAttribute(SUB_THREAD_CONTENT, "content");
                subThread.addAttribute(SUB_THREAD_HAS_IMAGE, false);
                subThread.addAttribute(SUB_THREAD_DATE, Timestamp.valueOf(LocalDateTime.now()));
                BaseDocument created = arango.createDocument(dbName, SUB_THREADS_COLLECTION_NAME, subThread);
                toBeDeleted.get(SUB_THREADS_COLLECTION_NAME).add(created.getKey());
                subThreadsKeys[i] = created.getKey();
            }
            TimeUnit.SECONDS.sleep(5);
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
            JSONObject responseJson = new JSONObject(new SearchThread().execute(makeRequest("ThreadForTestSearchApp")));
            assertEquals(200, responseJson.getInt("statusCode"));
            assertTrue(arango.viewExists(SearchApp.DB_NAME, SearchApp.getViewName(THREADS_COLLECTION_NAME)));
            assertTrue(arango.documentExists(SearchApp.DB_NAME, THREADS_COLLECTION_NAME, threads[0]));
            assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(THREAD_NAME).equals(threads[0]));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void SearchSubThread() {
        try {
            JSONObject responseJson = new JSONObject(new SearchSubThread().execute(makeRequest("ice cream")));
            assertEquals(200, responseJson.getInt("statusCode"));
            assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(SUB_THREAD_ID).equals(subThreadsKeys[1]));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }
}