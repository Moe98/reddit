package org.sab.chat.commands;

import org.json.JSONObject;
import org.sab.service.Command;

public class CreateDirectMessage extends Command {

    @Override
    public String execute(JSONObject request) {
        // Return SUCCESS
        return "{\"msg\":\"Hello World\", \"statusCode\": 200}";
        // Return ERROR Ex: 400 for bad request
//        return "{\"msg\":\"Hello World\", \"statusCode\": 400}";
    }

}
