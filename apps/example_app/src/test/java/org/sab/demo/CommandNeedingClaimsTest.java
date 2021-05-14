package org.sab.demo;

import org.json.JSONObject;
import org.junit.Test;
import org.sab.auth.AuthParamsHandler;
import org.sab.demo.commands.CommandNeedingAuth;
import org.sab.demo.commands.CommandNeedingClaims;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandNeedingClaimsTest {

    @Test
    public void willFailDueToUnAuthentication() {
        JSONObject request = new JSONObject().put("uriParams", new JSONObject()).put("methodType", "GET");
        AuthParamsHandler.putUnauthorizedParams(request);
        JSONObject response = new JSONObject(new CommandNeedingAuth().execute(request));
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode == 401 || statusCode == 403);
        assertTrue(response.getString("msg").startsWith("Unauthorized"));
    }

    @Test
    public void willSucceedAfterAuthentication() {
        JSONObject request = new JSONObject().put("uriParams", new JSONObject()).put("methodType", "GET");
        String username = "scale-a-bull";
        JSONObject claims = new JSONObject().put("username", username);
        AuthParamsHandler.putAuthorizedParams(request, claims);
        JSONObject response = new JSONObject(new CommandNeedingClaims().execute(request));
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(String.format("Hello %s!", username), response.getString("msg"));
    }

}
