package org.sab.demo;

import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;
import org.sab.demo.commands.GoodByeWorld;
import org.sab.demo.commands.HelloWorld;

/**
 * Unit test for Example App.
 */
public class ExampleAppTest {
    /**
     * Rigorous Test :-)
     * Amazing!
     * verification++
     */
    @Test
    public void helloWorldCorrectFunctionality() {

        HelloWorld h = new HelloWorld();
        String result = h.execute(new JSONObject());

        assertTrue(result.equals("{\"msg\":\"Hello World\"}"));
    }

    @Test
    public void goodByeWorldCorrectFunctionality() {

        GoodByeWorld h = new GoodByeWorld();
        String result = h.execute(new JSONObject());

        assertTrue(result.equals("{\"msg\":\"GoodBye World\"}"));
    }
}
