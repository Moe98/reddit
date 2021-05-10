package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.service.Command;

public class SignUp extends Command {

    @Override
    public String execute(JSONObject request) {
        return "{\"msg\":\"You just got in!\"}";
    }
}
