package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.service.Command;

public class ChooseProfilePhoto extends Command {

    @Override
    public String execute(JSONObject request) {
        return "{\"msg\":\"Mountains mountains!\"}";
    }
}
