package org.sab.tests;

import org.json.JSONObject;
import org.sab.auth.AuthParamsHandler;

import java.util.Map;

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

    public static JSONObject makeAuthorizedRequest(JSONObject body, String methodType, JSONObject uriParams, String username) {
        JSONObject unAuthorizedRequest = makeRequest(body, methodType, uriParams);
        return AuthParamsHandler.putAuthorizedParams(unAuthorizedRequest, Map.of("username", username));
    }
}
