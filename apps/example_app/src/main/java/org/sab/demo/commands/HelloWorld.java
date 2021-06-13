package org.sab.demo.commands;

import org.json.JSONObject;
import org.sab.service.Command;

import static org.sab.innerAppComm.Comm.updateRecommendation;


public class HelloWorld extends Command {

    @Override
    public String execute(JSONObject request) {
//        String[] arr = new String[0];
//        putMessageInQueue("hohoh",arr, "comment\423132");

        updateRecommendation("RECOMMENDATION_REQ", "hamoBeka");
        // Return SUCCESS
        return "{\"msg\":\"Hello World\", \"statusCode\": 200}";
        // Return ERROR Ex: 400 for bad request
//        return "{\"msg\":\"Hello World\", \"statusCode\": 400}";
    }
}