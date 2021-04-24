package org.sab.demo.commands;

import org.json.JSONObject;
import org.sab.service.Command;

import java.util.concurrent.Callable;

public class GoodByeWorld extends Command implements Callable<String> {

    @Override
    public String execute(JSONObject request) {
        return "{\"msg\":\"GoodBye World\"}";
    }
}
