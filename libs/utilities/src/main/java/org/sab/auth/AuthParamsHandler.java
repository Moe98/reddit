package org.sab.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.json.JSONObject;

import java.util.Map;

public class AuthParamsHandler {

    public static final String IS_AUTHENTICATED = "isAuthenticated";
    public static final String AUTHENTICATION_PARAMS = "authenticationParams";

    public static JSONObject decodeToken(String token) {
        JSONObject authenticationParams = new JSONObject();
        boolean isAuthenticated;
        try {
            Map<String, Object> claims = Jwt.verifyAndDecode(token);
            for (Map.Entry<String, Object> entry : claims.entrySet())
                authenticationParams.put(entry.getKey(), entry.getValue());
            isAuthenticated = true;
        } catch (JWTVerificationException jwtVerificationException) {
            isAuthenticated = false;
        }
        authenticationParams.put(IS_AUTHENTICATED, isAuthenticated);
        return authenticationParams;
    }

    public static JSONObject getUnauthorizedAuthParams() {
        return new JSONObject().put(IS_AUTHENTICATED, false);
    }

    public static JSONObject putUnauthorizedParams(JSONObject request) {
        JSONObject authParams = new JSONObject().put(IS_AUTHENTICATED, false);
        return request.put(AUTHENTICATION_PARAMS, authParams);
    }

    public static JSONObject putAuthorizedParams(JSONObject request) {
        JSONObject authParams = new JSONObject().put(IS_AUTHENTICATED, true);
        return request.put(AUTHENTICATION_PARAMS, authParams);
    }

    
    public static JSONObject putAuthorizedParams(JSONObject request, JSONObject claims) {
        JSONObject authParams = new JSONObject().put(IS_AUTHENTICATED, true);
        for (String key : claims.keySet())
            authParams.put(key, claims.get(key));
        return request.put(AUTHENTICATION_PARAMS, authParams);
    }
}
