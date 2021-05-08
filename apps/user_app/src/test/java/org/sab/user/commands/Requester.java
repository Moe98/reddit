package org.sab.user.commands;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.json.JSONObject;
import org.sab.service.authentication.Jwt;

import java.util.Map;

public class Requester {
    static JSONObject authenticationParams = new JSONObject();

    static public JSONObject makeRequest(JSONObject body, String methodType, JSONObject uriParams) {
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", methodType);
        request.put("uriParams", uriParams);
        request.put("authenticationParams", authenticationParams);
        return request;
    }

    static public JSONObject signUp(String username, String email, String password, String birthdate) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        body.put("birthdate", birthdate);
        JSONObject request = makeRequest(body, "POST", new JSONObject());
        return new JSONObject(new SignUp().execute(request));
    }

    static public JSONObject login(String username, String password) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        JSONObject request = makeRequest(body, "POST", new JSONObject());
        return new JSONObject(new Login().execute(request));
    }

    static public JSONObject getUser(String username) {
        JSONObject uriParams = new JSONObject().put("username", username);
        JSONObject request = makeRequest(null, "GET", uriParams);
        return new JSONObject(new GetUser().execute(request));
    }

    static public JSONObject updatePassword(String oldPassword, String newPassword) {
        JSONObject body = new JSONObject();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);
        JSONObject request = makeRequest(body, "PUT", new JSONObject());
        return new JSONObject(new UpdatePassword().execute(request));
    }

    static public JSONObject updateProfilePicture(String photoUrl) {
        JSONObject body = new JSONObject();
        body.put("photoUrl", photoUrl);

        JSONObject request = makeRequest(body, "PUT", new JSONObject());
        return new JSONObject(new UpdateProfilePhoto().execute(request));
    }

    static public JSONObject deleteProfilePicture() {
        JSONObject request = makeRequest(new JSONObject(), "DELETE", new JSONObject());
        return new JSONObject(new DeleteProfilePhoto().execute(request));
    }

    static public JSONObject deleteAccount(String password) {
        JSONObject body = new JSONObject();
        body.put("password", password);

        JSONObject request = makeRequest(body, "DELETE", new JSONObject());
        return new JSONObject(new DeleteAccount().execute(request));
    }

    public static void decodeToken(String token) {
        Jwt jwt = new Jwt();
        Boolean authenticated = false;
        try {
            Map<String, Object> claims = jwt.verifyAndDecode(token);
            authenticated = true;
            authenticationParams.put("username", (String) claims.get("username"));
            authenticationParams.put("jwt", token);
        } catch (JWTVerificationException jwtVerificationException) {
            System.out.println(jwtVerificationException.getMessage());
            authenticated = false;
        }
        authenticationParams.put("isAuthenticated", authenticated);
    }


}
