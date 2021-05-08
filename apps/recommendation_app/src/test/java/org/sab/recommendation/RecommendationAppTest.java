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
import org.sab.models.SubThread;
import org.sab.models.Thread;
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
    final public static String dbName = System.getenv("ARANGO_DB");
    final public static String threadsCollectionName = Thread.getCollectionName();
    final public static String threadName = Thread.getNameAttributeName();
    final public static String threadDescription = Thread.getDescriptionAttributeName();
    final public static String threadCreator = Thread.getCreatorAttributeName();
    final public static String threadFollowers = Thread.getNumOfFollowersAttributeName();
    final public static String threadDate = Thread.getDateCreatedAttributeName();
    final public static String subThreadsCollectionName = SubThread.getCollectionName();
    final public static String subThreadParentThread = SubThread.getParentThreadAttributeName();
    final public static String subThreadTitle = SubThread.getTitleAttributeName();
    final public static String subThreadCreator = SubThread.getCreatorAttributeName();
    final public static String subThreadLikes = SubThread.getLikesAttributeName();
    final public static String subThreadDislikes = SubThread.getDislikesAttributeName();
    final public static String subThreadContent = SubThread.getContentAttributeName();
    final public static String subThreadHasImage = SubThread.getHasImageAttributeName();
    final public static String subThreadDate = SubThread.getDateAttributeName();
    final public static String usersCollectionName = "Users";
    final public static String threadContainSubThreadCollectionName = "ThreadContainSubThread";
    final public static String userFollowUserCollectionName = "UserFollowUser";
    final public static String userFollowThreadCollectionName = "UserFollowThread";
    final public static int defaultRamQuota = 100;
    final public static String listingsBucketName = "Listings";
    final public static String listingsPopularThreadsKey = "popThreads";
    final public static String listingsPopularSubThreadsKey = "popSubThreads";
    final public static String recommendedSubThreadsBucketName = "RecommendedSubThreads";
    final public static String recommendedThreadsBucketName = "RecommendedThreads";
    final public static String recommendedUsersBucketName = "RecommendedUsers";
    private static Arango arango;
    private static Couchbase couchbase;
    private static Cluster cluster;
    private static HashMap<String, ArrayList<String>> toBeDeleted;
    private static String[] subThreads;
    private static String[] threads;
    private static String[] users;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            if (!arango.isConnected())
                arango.connect();
            couchbase = Couchbase.getInstance();
            cluster = couchbase.connect();

            toBeDeleted = new HashMap<>();
            toBeDeleted.put(usersCollectionName, new ArrayList<>());
            toBeDeleted.put(threadsCollectionName, new ArrayList<>());
            toBeDeleted.put(subThreadsCollectionName, new ArrayList<>());
            toBeDeleted.put(userFollowUserCollectionName, new ArrayList<>());
            toBeDeleted.put(userFollowThreadCollectionName, new ArrayList<>());
            toBeDeleted.put(threadContainSubThreadCollectionName, new ArrayList<>());

            // Dummy Data
            users = new String[10];
            threads = new String[]{"ComputersTEST98789", "PCPartsTEST98789", "MoviesTEST98789", "SeriesTEST98789"};
            String[] threadsDesc = new String[]{"all about computer", "all about computer parts", "all about movies", "all about tv series"};
            subThreads = new String[10];

            couchbase.createBucketIfNotExists(cluster, listingsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(cluster, recommendedSubThreadsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(cluster, recommendedThreadsBucketName, defaultRamQuota);
            couchbase.createBucketIfNotExists(cluster, recommendedUsersBucketName, defaultRamQuota);
            arango.createDatabaseIfNotExists(dbName);
            arango.createCollectionIfNotExists(dbName, threadsCollectionName, false);
            arango.createCollectionIfNotExists(dbName, subThreadsCollectionName, false);
            arango.createCollectionIfNotExists(dbName, usersCollectionName, false);
            arango.createCollectionIfNotExists(dbName, threadContainSubThreadCollectionName, true);
            arango.createCollectionIfNotExists(dbName, userFollowUserCollectionName, true);
            arango.createCollectionIfNotExists(dbName, userFollowThreadCollectionName, true);
            arango.createViewIfNotExists(dbName, RecommendationApp.getViewName(threadsCollectionName), threadsCollectionName, new String[]{threadName, threadDescription});
            arango.createViewIfNotExists(dbName, RecommendationApp.getViewName(subThreadsCollectionName), subThreadsCollectionName, new String[]{subThreadTitle, subThreadContent});

            for (int i = 0; i < users.length; i++) {
                if (arango.documentExists(dbName, usersCollectionName, "user" + i))
                    arango.deleteDocument(dbName, usersCollectionName, "user" + i);
                BaseDocument user = new BaseDocument();
                user.setKey("user" + i);
                arango.createDocument(dbName, usersCollectionName, user);
                toBeDeleted.get(usersCollectionName).add("user" + i);
                users[i] = "user" + i;
            }
            for (int i = 0; i < threads.length; i++) {
                if (arango.documentExists(dbName, threadsCollectionName, threads[i]))
                    arango.deleteDocument(dbName, threadsCollectionName, threads[i]);
                BaseDocument thread = new BaseDocument();
                thread.setKey(threads[i]);
                thread.addAttribute(threadDescription, threadsDesc[i]);
                thread.addAttribute(threadCreator, "hamada");
                thread.addAttribute(threadFollowers, 1000000000 + i);
                thread.addAttribute(threadDate, Timestamp.valueOf(LocalDateTime.now()));
                arango.createDocument(dbName, threadsCollectionName, thread);
                toBeDeleted.get(threadsCollectionName).add(threads[i]);
            }
            for (int i = 0; i < 5; i++) {
                BaseDocument subThread = new BaseDocument();
                subThread.addAttribute(subThreadParentThread, threads[0]);
                subThread.addAttribute(subThreadTitle, "title" + i);
                subThread.addAttribute(subThreadCreator, "hamada");
                subThread.addAttribute(subThreadLikes, 1000000000 + i);
                subThread.addAttribute(subThreadDislikes, 0);
                subThread.addAttribute(subThreadContent, "content");
                subThread.addAttribute(subThreadHasImage, false);
                subThread.addAttribute(subThreadDate, Timestamp.valueOf(LocalDateTime.now()));
                BaseDocument created1 = arango.createDocument(dbName, subThreadsCollectionName, subThread);
                toBeDeleted.get(subThreadsCollectionName).add(created1.getKey());
                subThreads[i] = created1.getKey();

                subThread = new BaseDocument();
                subThread.addAttribute(subThreadParentThread, threads[1]);
                subThread.addAttribute(subThreadTitle, "title" + i);
                subThread.addAttribute(subThreadCreator, "hamada");
                subThread.addAttribute(subThreadLikes, 1000000000 + i + 1);
                subThread.addAttribute(subThreadDislikes, 0);
                subThread.addAttribute(subThreadContent, "content");
                subThread.addAttribute(subThreadHasImage, false);
                subThread.addAttribute(subThreadDate, Timestamp.valueOf(LocalDateTime.now()));
                BaseDocument created2 = arango.createDocument(dbName, subThreadsCollectionName, subThread);
                toBeDeleted.get(subThreadsCollectionName).add(created2.getKey());
                subThreads[i + 5] = created2.getKey();
            }
            BaseEdgeDocument edgeDocument = new BaseEdgeDocument();
            edgeDocument.setFrom(usersCollectionName + "/" + users[0]);
            edgeDocument.setTo(threadsCollectionName + "/" + threads[0]);
            BaseEdgeDocument created = arango.createEdgeDocument(dbName, userFollowThreadCollectionName, edgeDocument);
            toBeDeleted.get(userFollowThreadCollectionName).add(created.getKey());
            for (int i = 0; i < subThreads.length; i++) {
                edgeDocument = new BaseEdgeDocument();
                edgeDocument.setTo(subThreadsCollectionName + "/" + subThreads[i]);
                if (i < 5)
                    edgeDocument.setFrom(threadsCollectionName + "/" + threads[0]);
                else
                    edgeDocument.setFrom(threadsCollectionName + "/" + threads[1]);
                created = arango.createEdgeDocument(dbName, threadContainSubThreadCollectionName, edgeDocument);
                toBeDeleted.get(threadContainSubThreadCollectionName).add(created.getKey());
            }
            for (int i = 0; i < 5; i++) {
                edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(usersCollectionName + "/" + users[0]);
                edgeDocument.setTo(usersCollectionName + "/" + users[i + 1]);
                created = arango.createEdgeDocument(dbName, userFollowUserCollectionName, edgeDocument);
                toBeDeleted.get(userFollowUserCollectionName).add(created.getKey());
            }
            for (int i = 1; i < 5; i++) {
                edgeDocument = new BaseEdgeDocument();
                edgeDocument.setFrom(usersCollectionName + "/" + users[i]);
                edgeDocument.setTo(usersCollectionName + "/" + users[i + 5]);
                created = arango.createEdgeDocument(dbName, userFollowUserCollectionName, edgeDocument);
                toBeDeleted.get(userFollowUserCollectionName).add(created.getKey());
            }
            edgeDocument = new BaseEdgeDocument();
            edgeDocument.setFrom(usersCollectionName + "/" + users[1]);
            edgeDocument.setTo(usersCollectionName + "/" + users[users.length - 1]);
            created = arango.createEdgeDocument(dbName, userFollowUserCollectionName, edgeDocument);
            toBeDeleted.get(userFollowUserCollectionName).add(created.getKey());
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
            couchbase.deleteDocument(cluster, recommendedThreadsBucketName, users[0]);
            couchbase.deleteDocument(cluster, listingsBucketName, listingsPopularThreadsKey);
            couchbase.deleteDocument(cluster, listingsBucketName, listingsPopularSubThreadsKey);
            couchbase.deleteDocument(cluster, recommendedSubThreadsBucketName, users[0]);
            couchbase.deleteDocument(cluster, recommendedUsersBucketName, users[0]);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
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
        assertTrue(responseJson.getJSONArray("data").getString(0).equals(users[users.length - 1]));
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
        assertTrue(responseJson.getJSONArray("data").getString(0).equals(users[users.length - 1]));
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
        } catch (IOException | ClassNotFoundException e) {
            fail(e.getMessage());
        }
    }
}
