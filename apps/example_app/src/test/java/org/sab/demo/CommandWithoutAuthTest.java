package org.sab.demo;

import org.json.JSONObject;
import org.junit.Test;
import org.sab.demo.commands.CommandWithoutAuth;

import static org.junit.Assert.assertEquals;

public class CommandWithoutAuthTest {

    @Test
    public void willSucceedWithoutAuth() {
        JSONObject request = new JSONObject().put("uriParams", new JSONObject()).put("methodType", "GET");
        JSONObject response = new JSONObject(new CommandWithoutAuth().execute(request));
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("I don't need authentication!", response.getString("msg"));
    }


}
