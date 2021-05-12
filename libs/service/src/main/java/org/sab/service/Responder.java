package org.sab.service;

import org.json.JSONArray;
import org.json.JSONObject;

public class Responder {
    private Responder() {
    }

    public static String makeErrorResponse(String msg, int statusCode) {
        JSONObject error = new JSONObject().put("msg", msg).put("statusCode", statusCode);
        return error.toString();
    }

    public static String makeDataResponse(JSONObject data) {
        JSONObject response = new JSONObject().put("data", data);
        response.put("statusCode", 200);
        return response.toString();
    }

    public static String makeMsgResponse(String msg) {
        JSONObject response = new JSONObject().put("msg", msg);
        response.put("statusCode", 200);
        return response.toString();
    }

    public static JSONObject makeDataResponse(JSONArray data) {
        JSONObject response = new JSONObject().put("data", data);
        response.put("statusCode", 200);
        return response;
    }
}
