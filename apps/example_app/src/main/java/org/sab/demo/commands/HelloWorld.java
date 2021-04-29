package org.sab.demo.commands;

import org.json.JSONObject;
import org.sab.service.Command;

public class HelloWorld extends Command {

    @Override
    public String execute(JSONObject request) {
        return "{\"msg\":\"Hello World\"}";
    }
}