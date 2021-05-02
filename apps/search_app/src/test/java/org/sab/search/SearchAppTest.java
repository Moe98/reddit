package org.sab.search;

import com.arangodb.ArangoDB;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.arango.Arango;
import org.sab.search.commands.SearchSubThread;
import org.sab.search.commands.SearchThread;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class SearchAppTest {
    private static Arango arango;
    private static ArangoDB arangoDB;
    private static HashMap<String, ArrayList<String>> toBeDeleted;
    private static String[] subThreads;
    private static String[] threads;

    @BeforeClass
    public static void setUp() {
        try {
            arango = Arango.getInstance();
            arangoDB = arango.connect();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDown() {
        arango.disconnect(arangoDB);
    }

    @Test
    public void SearchThread() {
        try {
            JSONObject responseJson = new JSONObject(new SearchThread().execute(new JSONObject().put("body", new JSONObject().put("searchText", "ThreadForTestSearch"))));
            assertEquals(200, responseJson.getInt("statusCode"));
            assertTrue(responseJson.getString("msg").equals("No Result"));
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void SearchSubThread() {
        try {
            JSONObject responseJson = new JSONObject(new SearchSubThread().execute(new JSONObject().put("body", new JSONObject().put("searchText", "ice cream"))));
            assertEquals(200, responseJson.getInt("statusCode"));
            assertTrue(responseJson.getString("msg").equals("No Result"));
        } catch (JSONException e) {
            fail(e.getMessage());
        }

    }
}