package org.sab.search;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.search.commands.SearchSubThread;
import org.sab.search.commands.SearchThread;
import org.sab.models.Thread;
import org.sab.models.SubThread;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SearchAppTest {
    private static Arango arango;
    private static ArangoDB arangoDB;
    private static HashMap<String, ArrayList<String>> toBeDeleted;
    final private static String dbName = System.getenv("ARANGO_DB");
    final private static String threadsCollectionName = Thread.getCollectionName();
    final private static String subThreadsCollectionName = SubThread.getCollectionName();

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            arango.createDatabaseIfNotExists(arangoDB, dbName);
            arango.createCollectionIfNotExists(arangoDB, dbName, threadsCollectionName, false);
            arango.createCollectionIfNotExists(arangoDB, dbName, subThreadsCollectionName, false);

            // Dummy Data
            toBeDeleted = new HashMap<>();
            toBeDeleted.put(threadsCollectionName, new ArrayList<>());
            toBeDeleted.put(subThreadsCollectionName, new ArrayList<>());

            String[] threads = new String[]{"ThreadForTestSearchApp", "ThreadTestTestSearchApp"};
            String[] subThreadsTitles = new String[]{"i love scalable", "i went to eat ice cream"};
            for (String s : threads) {
                BaseDocument thread = new BaseDocument();
                thread.setKey(s);
                thread.addAttribute(Thread.getDescriptionAttributeName(), "desc");
                thread.addAttribute(Thread.getCreatorAttributeName(), "hamada");
                thread.addAttribute(Thread.getNumOfFollowersAttributeName(), 0);
                thread.addAttribute(Thread.getDateCreatedAttributeName(), Timestamp.valueOf(LocalDateTime.now()));
                arango.createDocument(arangoDB, dbName, threadsCollectionName, thread);
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
                BaseDocument created = arango.createDocument(arangoDB, dbName, subThreadsCollectionName, subThread);
                toBeDeleted.get(subThreadsCollectionName).add(created.getKey());
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        try {
            toBeDeleted.forEach((key, value) -> {
                for (String _key : value) {
                    arango.deleteDocument(arangoDB, dbName, key, _key);
                }
            });
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            arango.disconnect(arangoDB);
        }
    }

    @Test
    public void SearchThread() {
        try {
            JSONObject responseJson = new JSONObject(new SearchThread().execute(new JSONObject().put("body", new JSONObject().put("searchKeyword", "ThreadForTestSearchApp"))));
            assertEquals("test", responseJson.getString("msg"));
            assertEquals(200, responseJson.getInt("statusCode"));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void SearchSubThread() {
        try {
            JSONObject responseJson = new JSONObject(new SearchSubThread().execute(new JSONObject().put("body", new JSONObject().put("searchKeywords", "ice cream"))));
            assertEquals(200, responseJson.getInt("statusCode"));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }
}