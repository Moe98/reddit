package org.sab.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.json.JSONObject;

import java.util.Map;

public class AuthParamsHandler {
    public static JSONObject decodeToken(String token) {
        JSONObject authenticationParams = new JSONObject();
        boolean authenticated;
        try {
            Map<String, Object> claims = Jwt.verifyAndDecode(token);
            authenticated = true;
            // for(Map.Entry<String,Object> entry:claims)
            // authenticationParams.put(entry.getKey(),entry.getValue();
            authenticationParams.put("username", claims.get("username"));
            authenticationParams.put("jwt", token);
        } catch (JWTVerificationException jwtVerificationException) {
            authenticated = false;
        }
        authenticationParams.put("isAuthenticated", authenticated);
        return authenticationParams;
    }

    public static JSONObject getUnauthorizedAuthParams() {
        return new JSONObject().put("isAuthenticated", false);
    }
}
