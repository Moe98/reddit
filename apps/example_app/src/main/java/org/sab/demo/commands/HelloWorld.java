package org.sab.demo.commands;

import org.json.JSONObject;
import org.sab.service.Command;

public class HelloWorld extends Command {

    @Override
    public String execute(JSONObject request) {
        // Return SUCCESS
        return "{\"msg\":\"Hello Worlddd\", \"statusCode\": 200}";
        // Return ERROR Ex: 400 for bad request
//        return "{\"msg\":\"Hello Worlddd\", \"statusCode\": 400}";
    }
}