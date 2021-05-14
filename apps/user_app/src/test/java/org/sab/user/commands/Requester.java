package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.auth.AuthParamsHandler;

import java.io.IOException;

public class Requester {
    Requester() {
    }

    static JSONObject authenticationParams = AuthParamsHandler.getUnauthorizedAuthParams();

    private static JSONObject makeRequest(JSONObject body, String methodType, JSONObject uriParams) {
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", methodType);
        request.put("uriParams", uriParams);
        request.put("authenticationParams", authenticationParams);
        return request;
    }

    public static JSONObject signUp(String username, String email, String password, String birthdate) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        body.put("birthdate", birthdate);
        JSONObject request = makeRequest(body, "POST", new JSONObject());
        return new JSONObject(new SignUp().execute(request));
    }

    public static JSONObject login(String username, String password) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);
        JSONObject request = makeRequest(body, "POST", new JSONObject());
        return new JSONObject(new Login().execute(request));
    }

    public static JSONObject getUser() {
        JSONObject request = makeRequest(null, "GET", new JSONObject());
        return new JSONObject(new ViewMyProfile().execute(request));
    }

    public static JSONObject viewAnotherProfile(String username) {
        JSONObject uriParams = new JSONObject().put("username", username);
        JSONObject request = makeRequest(null, "GET", uriParams);
        return new JSONObject(new ViewAnotherProfile().execute(request));
    }

    public static JSONObject updatePassword(String oldPassword, String newPassword) {
        JSONObject body = new JSONObject();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);
        JSONObject request = makeRequest(body, "PUT", new JSONObject());
        return new JSONObject(new UpdatePassword().execute(request));
    }

    static public JSONObject updateProfilePicture() throws IOException {
        JSONObject request = makeRequest(new JSONObject(), "PUT", new JSONObject());
        JSONObject files = new JSONObject().put("image", org.sab.minio.FileSimulation.generateImageJson());
        request.put("files", files);
        return new JSONObject(new UpdateProfilePhoto().execute(request));
    }

    public static JSONObject deleteProfilePicture() {
        JSONObject request = makeRequest(new JSONObject(), "DELETE", new JSONObject());
        return new JSONObject(new DeleteProfilePhoto().execute(request));
    }

    public static JSONObject deleteAccount(String password) {
        JSONObject body = new JSONObject();
        body.put("password", password);

        JSONObject request = makeRequest(body, "DELETE", new JSONObject());
        return new JSONObject(new DeleteAccount().execute(request));
    }


}
