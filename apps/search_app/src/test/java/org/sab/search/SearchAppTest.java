package org.sab.search;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.sab.search.commands.SearchSubThread;
import org.sab.search.commands.SearchThread;

import static org.junit.Assert.*;

public class SearchAppTest {
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