package org.sab.recommendation;


import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.couchbase.client.java.Cluster;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.couchbase.Couchbase;
import org.sab.recommendation.commands.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.*;

public class RecommendationAppTest {
    private static Arango arango;
    private static ArangoDB arangoDB;
    private static HashMap<String, ArrayList<String>> toBeDeleted;
    private static String[] subThreads;
    private static String[] threads;
    private static String[] users;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();

            toBeDeleted = new HashMap<>();
            toBeDeleted.put("Users", new ArrayList<>());
            toBeDeleted.put("Threads", new ArrayList<>());
            toBeDeleted.put("SubThreads", new ArrayList<>());
            toBeDeleted.put("UserFollowUser", new ArrayList<>());
            toBeDeleted.put("UserFollowThread", new ArrayList<>());
            toBeDeleted.put("ThreadContainSubThread", new ArrayList<>());

            // Dummy Data
            users = new String[10];
            threads = new String[]{"ComputersTEST98789", "PCPartsTEST98789", "MoviesTEST98789", "SeriesTEST98789"};
            String[] threadsDesc = new String[]{"all about computer", "all about computer parts", "all about movies", "all about tv series"};
            subThreads = new String[10];
            if (!arango.databaseExists(arangoDB, System.getenv("ARANGO_DB"))) {
                arango.createDatabase(arangoDB, System.getenv("ARANGO_DB"));
            }
            if (!arango.collectionExists(arangoDB, System.getenv("ARANGO_DB"), "Threads")) {
                arango.createCollection(arangoDB, System.getenv("ARANGO_DB"), "Threads", false);
            }
            if (!arango.collectionExists(arangoDB, System.getenv("ARANGO_DB"), "SubThreads")) {
                arango.createCollection(arangoDB, System.getenv("ARANGO_DB"), "SubThreads", false);
            }
            if (!arango.collectionExists(arangoDB, System.getenv("ARANGO_DB"), "Users")) {
                arango.createCollection(arangoDB, System.getenv("ARANGO_DB"), "Users", false);
            }
            if (!arango.collectionExists(arangoDB, System.getenv("ARANGO_DB"), "UserFollowUser")) {
                arango.createCollection(arangoDB, System.getenv("ARANGO_DB"), "UserFollowUser", true);
            }
            if (!arango.collectionExists(arangoDB, System.getenv("ARANGO_DB"), "UserFollowThread")) {
                arango.createCollection(arangoDB, System.getenv("ARANGO_DB"), "UserFollowThread", true);
            }
            if (!arango.collectionExists(arangoDB, System.getenv("ARANGO_DB"), "ThreadContainSubThread")) {
                arango.createCollection(arangoDB, System.getenv("ARANGO_DB"), "ThreadContainSubThread", true);
            }
            for (int i = 0; i < users.length; i++) {
                BaseDocument user = new BaseDocument();
                user.setKey("user" + i);
                arango.createDocument(arangoDB, System.getenv("ARANGO_DB"), "Users", user);
                toBeDeleted.get("Users").add("user" + i);
                users[i] = "user" + i;
            }
            for (int i = 0; i < threads.length; i++) {
                BaseDocument thread = new BaseDocument();
                thread.setKey(threads[i]);
                thread.addAttribute("Title", threadsDesc[i]);
                thread.addAttribute("Creator", "hamada");
                thread.addAttribute("NumOfFollowers", 1000000000 + i);
                thread.addAttribute("DateCreated", Timestamp.valueOf(LocalDateTime.now()));
                arango.createDocument(arangoDB, System.getenv("ARANGO_DB"), "Threads", thread);
                toBeDeleted.get("Threads").add(threads[i]);
            }
            for (int i = 0; i < 5; i++) {
                BaseDocument subThread = new BaseDocument();
                subThread.addAttribute("ParentThread", threads[0]);
                subThread.addAttribute("Title", "title" + i);
                subThread.addAttribute("Creator", "hamada");
                subThread.addAttribute("Likes", 1000000000 + i);
                subThread.addAttribute("Dislikes", 0);
                subThread.addAttribute("Content", "content");
                subThread.addAttribute("HasImage", false);
                subThread.addAttribute("Time", Timestamp.valueOf(LocalDateTime.now()));
                BaseDocument created1 = arango.createDocument(arangoDB, System.getenv("ARANGO_DB"), "SubThreads", subThread);
                toBeDeleted.get("SubThreads").add(created1.getKey());
                subThreads[i] = created1.getKey();

                subThread = new BaseDocument();
                subThread.addAttribute("ParentThread", threads[1]);
                subThread.addAttribute("Title", "title" + i);
                subThread.addAttribute("Creator", "hamada");
                subThread.addAttribute("Likes", 1000000000 + i + 1);
                subThread.addAttribute("Dislikes", 0);
                subThread.addAttribute("Content", "content");
                subThread.addAttribute("HasImage", false);
                subThread.addAttribute("Time", Timestamp.valueOf(LocalDateTime.now()));
                BaseDocument created2 = arango.createDocument(arangoDB, System.getenv("ARANGO_DB"), "SubThreads", subThread);
                toBeDeleted.get("SubThreads").add(created2.getKey());
                subThreads[i + 5] = created2.getKey();
            }
            BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
            edgeDocument.setFrom("Users/" + users[0]);
            edgeDocument.setTo("Threads/" + threads[0]);
            BaseEdgeDocument created = arango.createEdgeDocument(arangoDB, System.getenv("ARANGO_DB"), "UserFollowThread", edgeDocument);
            toBeDeleted.get("UserFollowThread").add(created.getKey());
            for (int i = 0; i < subThreads.length; i++) {
                edgeDocument = new BaseEdgeDocument();
                edgeDocument.setTo("SubThreads/" + subThreads[i]);
                if (i < 5)
                    edgeDocument.setFrom("Threads/" + threads[0]);
                else
                    edgeDocument.setFrom("Threads/" + threads[1]);
                created = arango.createEdgeDocument(arangoDB, System.getenv("ARANGO_DB"), "ThreadContainSubThread", edgeDocument);
                toBeDeleted.get("ThreadContainSubThread").add(created.getKey());
            }
            for (int i = 0; i < 5; i++) {
                edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom("Users/" + users[0]);
                edgeDocument.setTo("Users/" + users[i + 1]);
                created = arango.createEdgeDocument(arangoDB, System.getenv("ARANGO_DB"), "UserFollowUser", edgeDocument);
                toBeDeleted.get("UserFollowUser").add(created.getKey());
            }
            for (int i = 1; i < 5; i++) {
                edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom("Users/" + users[i]);
                edgeDocument.setTo("Users/" + users[i + 5]);
                created = arango.createEdgeDocument(arangoDB, System.getenv("ARANGO_DB"), "UserFollowUser", edgeDocument);
                toBeDeleted.get("UserFollowUser").add(created.getKey());
            }
            edgeDocument = new BaseEdgeDocument();
            edgeDocument.setFrom("Users/" + users[1]);
            edgeDocument.setTo("Users/" + users[users.length-1]);
            created = arango.createEdgeDocument(arangoDB, System.getenv("ARANGO_DB"), "UserFollowUser", edgeDocument);
            toBeDeleted.get("UserFollowUser").add(created.getKey());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        Couchbase couchbase = Couchbase.getInstance();
        Cluster cluster = couchbase.connect();
        try {
            toBeDeleted.forEach((key, value) -> {
                for (String _key : value) {
                    arango.deleteDocument(arangoDB, System.getenv("ARANGO_DB"), key, _key);
                }
            });
            couchbase.deleteDocument(cluster, "RecommendedThreads", users[0]);
            couchbase.deleteDocument(cluster, "Listings", "popThreads");
            couchbase.deleteDocument(cluster, "Listings", "popSubThreads");
            couchbase.deleteDocument(cluster, "RecommendedSubThreads", users[0]);
            couchbase.deleteDocument(cluster, "RecommendedUsers", users[0]);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            arango.disconnect(arangoDB);
            couchbase.disconnect(cluster);
        }
    }

    @Test
    public void GetPopularSubThreads() {
        new UpdatePopularSubThreads().execute(new JSONObject());
        JSONObject responseJson = new JSONObject(new GetPopularSubThreads().execute(new JSONObject()));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString("_key").equals(subThreads[subThreads.length - 1]));
    }

    @Test
    public void GetPopularThreads() {
        new UpdatePopularThreads().execute(new JSONObject());
        JSONObject responseJson = new JSONObject(new GetPopularThreads().execute(new JSONObject()));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString("_key").equals(threads[threads.length - 1]));
    }

    @Test
    public void GetRecommendedSubThreads() {
        new UpdateRecommendedSubThreads().execute(new JSONObject().put("body", new JSONObject().put("username", users[0])));
        JSONObject responseJson = new JSONObject(new GetRecommendedSubThreads().execute(new JSONObject().put("body", new JSONObject().put("username", users[0]))));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString("_key").equals(subThreads[4]));
    }

    @Test
    public void GetRecommendedThreads() {
        new UpdateRecommendedThreads().execute(new JSONObject().put("body", new JSONObject().put("username", users[0])));
        JSONObject responseJson = new JSONObject(new GetRecommendedThreads().execute(new JSONObject().put("body", new JSONObject().put("username", users[0]))));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").length() != 0);
    }

    @Test
    public void GetRecommendedUsers() {
        new UpdateRecommendedUsers().execute(new JSONObject().put("body", new JSONObject().put("username", users[0])));
        JSONObject responseJson = new JSONObject(new GetRecommendedUsers().execute(new JSONObject().put("body", new JSONObject().put("username", users[0]))));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getString(0).equals(users[users.length-1]));
    }

    @Test
    public void UpdatePopularSubThreads() {
        JSONObject responseJson = new JSONObject(new UpdatePopularSubThreads().execute(new JSONObject()));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString("_key").equals(subThreads[subThreads.length - 1]));
    }

    @Test
    public void UpdatePopularThreads() {
        JSONObject responseJson = new JSONObject(new UpdatePopularThreads().execute(new JSONObject()));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString("_key").equals(threads[threads.length - 1]));
    }

    @Test
    public void UpdateRecommendedSubThreads() {
        JSONObject responseJson = new JSONObject(new UpdateRecommendedSubThreads().execute(new JSONObject().put("body", new JSONObject().put("username", users[0]))));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString("_key").equals(subThreads[4]));
    }

    @Test
    public void UpdateRecommendedThreads() {
        JSONObject responseJson = new JSONObject(new UpdateRecommendedThreads().execute(new JSONObject().put("body", new JSONObject().put("username", users[0]))));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").length() != 0);
    }

    @Test
    public void UpdateRecommendedUsers() {
        JSONObject responseJson = new JSONObject(new UpdateRecommendedUsers().execute(new JSONObject().put("body", new JSONObject().put("username", users[0]))));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getString(0).equals(users[users.length-1]));
    }

    @Test
    public void TestConfigMap() {
        try {
            final InputStream configMapStream = getClass().getClassLoader().getResourceAsStream(new RecommendationApp().getConfigMapPath());
            final Properties properties = new Properties();
            properties.load(configMapStream);

            for (final String key : properties.stringPropertyNames()) {
                Class.forName(properties.get(key).toString());
            }
        } catch (IOException | ClassNotFoundException e){
            fail(e.getMessage());
        }
    }
}