package org.sab.demo.commands;

import org.json.JSONObject;
import org.sab.service.Command;

import java.util.concurrent.Callable;

public class HelloWorld extends Command implements Callable<String> {

    @Override
    public String execute(JSONObject request) {
        return "{\"msg\":\"Hello World\"}";
    }
}