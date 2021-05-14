package org.sab.demo;

import org.json.JSONObject;
import org.junit.Test;
import org.sab.auth.AuthParamsHandler;
import org.sab.demo.commands.CommandNeedingAuth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandNeedingAuthTest {

    @Test
    public void willFailDueToUnAuthentication() {
        JSONObject request = new JSONObject().put("uriParams", new JSONObject()).put("methodType", "GET");
        AuthParamsHandler.putUnauthorizedParams(request);
        JSONObject response = new JSONObject(new CommandNeedingAuth().execute(request));
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode == 401 || statusCode == 403);
    }

    @Test
    public void willSucceedAfterAuthentication() {
        JSONObject request = new JSONObject().put("uriParams", new JSONObject()).put("methodType", "GET");
        AuthParamsHandler.putAuthorizedParams(request);
        JSONObject response = new JSONObject(new CommandNeedingAuth().execute(request));
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("Authentication successful!", response.getString("msg"));
    }
}
