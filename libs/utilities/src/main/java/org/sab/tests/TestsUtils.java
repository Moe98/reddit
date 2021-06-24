package org.sab.tests;

import org.json.JSONObject;
import org.sab.auth.AuthParamsHandler;

public class TestsUtils {
    public static JSONObject makeRequest(JSONObject body, String methodType, JSONObject uriParams) {
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", methodType);
        request.put("uriParams", uriParams);
        return request;
    }

    public static JSONObject makeAuthorizedRequest(JSONObject body, String methodType, JSONObject uriParams) {
        JSONObject unAuthorizedRequest = makeRequest(body, methodType, uriParams);
        return AuthParamsHandler.putAuthorizedParams(unAuthorizedRequest);
    }
}
