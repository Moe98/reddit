package org.sab.recommendation;


import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
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
    final private static String AUTHENTICATION_PARAMS = "authenticationParams";
    final private static String AUTHENTICATED = "isAuthenticated";
    static String dbName = RecommendationCommand.DB_NAME;
    static String threadsCollectionName = RecommendationCommand.THREADS_COLLECTION_NAME;
    static String threadName = RecommendationCommand.THREAD_NAME;
    static String threadDescription = RecommendationCommand.THREAD_DESCRIPTION;
    static String threadCreator = RecommendationCommand.THREAD_CREATOR;
    static String threadFollowers = RecommendationCommand.THREAD_FOLLOWERS;
    static String threadDate = RecommendationCommand.THREAD_DATE;
    static String subThreadsCollectionName = RecommendationCommand.SUB_THREADS_COLLECTION_NAME;
    static String subThreadId = RecommendationCommand.SUB_THREAD_ID;
    static String subThreadParentThread = RecommendationCommand.SUB_THREAD_PARENT_THREAD;
    static String subThreadTitle = RecommendationCommand.SUB_THREAD_TITLE;
    static String subThreadCreator = RecommendationCommand.SUB_THREAD_CREATOR;
    static String subThreadLikes = RecommendationCommand.SUB_THREAD_LIKES;
    static String subThreadDislikes = RecommendationCommand.SUB_THREAD_DISLIKES;
    static String subThreadContent = RecommendationCommand.SUB_THREAD_CONTENT;
    static String subThreadHasImage = RecommendationCommand.SUB_THREAD_HAS_IMAGE;
    static String subThreadDate = RecommendationCommand.SUB_THREAD_DATE;
    static String usersCollectionName = RecommendationCommand.USERS_COLLECTION_NAME;
    static String threadContainSubThreadCollectionName = RecommendationCommand.THREAD_CONTAIN_SUB_THREAD_COLLECTION_NAME;
    static String userFollowUserCollectionName = RecommendationCommand.USER_FOLLOW_USER_COLLECTION_NAME;
    static String userFollowThreadCollectionName = RecommendationCommand.USER_FOLLOW_THREAD_COLLECTION_NAME;
    static String listingsBucketName = RecommendationCommand.LISTINGS_BUCKET_NAME;
    static String listingsPopularThreadsKey = RecommendationCommand.LISTINGS_POPULAR_THREADS_KEY;
    static String listingsPopularSubThreadsKey = RecommendationCommand.LISTINGS_POPULAR_SUB_THREADS_KEY;
    static String recommendedSubThreadsBucketName = RecommendationCommand.RECOMMENDED_SUB_THREADS_BUCKET_NAME;
    static String recommendedThreadsBucketName = RecommendationCommand.RECOMMENDED_THREADS_BUCKET_NAME;
    static String recommendedUsersBucketName = RecommendationCommand.RECOMMENDED_USERS_BUCKET_NAME;
    static Arango arango;
    static Couchbase couchbase;
    static HashMap<String, ArrayList<String>> toBeDeleted;
    static String[] subThreads;
    static String[] threads;
    static String[] users;
    static JSONObject authentication;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arango.connectIfNotConnected();
            couchbase = Couchbase.getInstance();
            couchbase.connectIfNotConnected();

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

            RecommendationApp.dbInit();

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
            couchbase.deleteDocument(listingsBucketName, listingsPopularSubThreadsKey);
            couchbase.deleteDocument(listingsBucketName, listingsPopularThreadsKey);
            couchbase.deleteDocument(recommendedThreadsBucketName, users[0]);
            couchbase.deleteDocument(recommendedSubThreadsBucketName, users[0]);
            couchbase.deleteDocument(recommendedUsersBucketName, users[0]);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            arango.disconnect();
            couchbase.disconnect();
        }
    }

    private static JSONObject makeRequest(String methodType) {
        JSONObject request = new JSONObject();
        authentication = new JSONObject();
        authentication.put(AUTHENTICATED, true);
        authentication.put(RecommendationCommand.USERNAME, users[0]);
        request.put(AUTHENTICATION_PARAMS, authentication);
        request.put("methodType", methodType);
        request.put("uriParams", new JSONObject());
        return request;
    }

    @Test
    public void GetPopularSubThreads() {
        new UpdatePopularSubThreads().execute();
        JSONObject responseJson = new JSONObject(new GetPopularSubThreads().execute());
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(subThreadId).equals(subThreads[subThreads.length - 1]));
    }

    @Test
    public void GetPopularThreads() {
        new UpdatePopularThreads().execute();
        JSONObject responseJson = new JSONObject(new GetPopularThreads().execute());
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(threadName).equals(threads[threads.length - 1]));
    }

    @Test
    public void GetRecommendedSubThreads() {
        new UpdateRecommendedSubThreads().execute(makeRequest("PUT"));
        JSONObject responseJson = new JSONObject(new GetRecommendedSubThreads().execute(makeRequest("GET")));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(subThreadId).equals(subThreads[4]));
    }

    @Test
    public void GetRecommendedThreads() {
        new UpdateRecommendedThreads().execute(makeRequest("PUT"));
        JSONObject responseJson = new JSONObject(new GetRecommendedThreads().execute(makeRequest("GET")));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").length() != 0);
    }

    @Test
    public void GetRecommendedUsers() {
        new UpdateRecommendedUsers().execute(makeRequest("PUT"));
        JSONObject responseJson = new JSONObject(new GetRecommendedUsers().execute(makeRequest("GET")));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getString(0).equals(users[users.length - 1]));
    }

    @Test
    public void UpdatePopularSubThreads() {
        JSONObject responseJson = new JSONObject(new UpdatePopularSubThreads().execute());
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(subThreadId).equals(subThreads[subThreads.length - 1]));
    }

    @Test
    public void UpdatePopularThreads() {
        JSONObject responseJson = new JSONObject(new UpdatePopularThreads().execute());
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(threadName).equals(threads[threads.length - 1]));
    }

    @Test
    public void UpdateRecommendedSubThreads() {
        JSONObject responseJson = new JSONObject(new UpdateRecommendedSubThreads().execute(makeRequest("PUT")));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").getJSONObject(0).getString(subThreadId).equals(subThreads[4]));
    }

    @Test
    public void UpdateRecommendedThreads() {
        JSONObject responseJson = new JSONObject(new UpdateRecommendedThreads().execute(makeRequest("PUT")));
        assertEquals(200, responseJson.getInt("statusCode"));
        assertTrue(responseJson.getJSONArray("data").length() != 0);
    }

    @Test
    public void UpdateRecommendedUsers() {
        JSONObject responseJson = new JSONObject(new UpdateRecommendedUsers().execute(makeRequest("PUT")));
        assertEquals("", responseJson.getString("msg"));
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
