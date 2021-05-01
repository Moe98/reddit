package org.sab.user;

import org.json.JSONObject;

public class Responder {
    private Responder() {
    }

    public static JSONObject makeErrorResponse(String msg, int statusCode) {
        JSONObject error = new JSONObject().put("msg", msg).put("statusCode", statusCode);
        return error;
    }

    public static JSONObject makeDataResponse(JSONObject data) {
        JSONObject response = new JSONObject().put("data", data);
        response.put("statusCode", 200);
        return response;
    }
}
