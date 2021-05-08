package org.sab.user.commands;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.functions.Auth;
import org.sab.functions.Utilities;
import org.sab.service.authentication.Jwt;
import org.sab.user.UserApp;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommandsTest {

    static String username = "scale-a-bull" + new Date().getTime();
    static String password = "12345678";
    static String token;
    static JSONObject authenticationParams = new JSONObject();
    JSONObject makeRequest(JSONObject body, String methodType, JSONObject uriParams) {
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", methodType);
        request.put("uriParams", uriParams);
        request.put("authenticationParams",authenticationParams);
        return request;
    }

    @BeforeClass
    public static void connectToDbs() {
        try {
            UserApp.dbInit();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    public JSONObject makeGETRequest(String username) {
        JSONObject uriParams = new JSONObject().put("username", username);
        JSONObject request = makeRequest(null, "GET", uriParams);
        return request;
    }

    public JSONObject getUserRequest() {
        JSONObject request = makeGETRequest(username);
        GetUser getUserCommand = new GetUser();
        JSONObject response = new JSONObject(getUserCommand.execute(request));
        return response;
    }
    public void decodeToken(String token){
        Jwt jwt = new Jwt();
        Boolean authenticated = false;
        try {
            Map<String, Object> claims = jwt.verifyAndDecode(token);
            authenticated = true;
            authenticationParams.put("username",(String) claims.get("username"));
            authenticationParams.put("jwt",token);
        } catch (JWTVerificationException jwtVerificationException) {
            System.out.println(jwtVerificationException.getMessage());
            authenticated = false;
        }
        authenticationParams.put("isAuthenticated",authenticated);
    }

    @Test
    public void a_SignUpCreatesAnEntryInDB() {

        JSONObject body = new JSONObject();

        String email = username + "@gmail.com";
        String birthdate = "1997-12-14";
        body.put("username", username);
        body.put("password", password);
        body.put("email", email);
        body.put("birthdate", birthdate);

        JSONObject request = makeRequest(body, "POST", new JSONObject());
        SignUp signUpCommand = new SignUp();
        JSONObject response = new JSONObject(signUpCommand.execute(request));
        System.out.println(response);

        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(data.getString("username"), username);
        assertEquals(data.getString("birthdate"), birthdate);
        assertEquals(data.getString("email"), email);

    }
    @Test
    public void b_Login() {

        JSONObject body = new JSONObject();

        body.put("username", username);
        body.put("password", password);
        JSONObject request = makeRequest(body, "POST", new JSONObject());

        Login loginCommand = new Login();
        JSONObject response = new JSONObject(loginCommand.execute(request));

        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"), "Login Successful!");
        token = response.getString("token");
        decodeToken(token);
        System.out.println(authenticationParams.toString());
    }
    @Test
    public void c_GetUser() {
        System.out.println(authenticationParams.toString());

        JSONObject response = getUserRequest();
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(data.getString("username"), username);
        assertTrue(Auth.verifyHash(password, data.getString("password")));

    }

    @Test
    public void d_updatePassword() {

        String newPassword = "123456";
        JSONObject body = new JSONObject();

        body.put("username", username);
        body.put("newPassword", "123456");
        body.put("oldPassword", password);

        JSONObject request = makeRequest(body, "PUT", new JSONObject());
        UpdatePassword updatePasswordCommand = new UpdatePassword();

        JSONObject response = new JSONObject(updatePasswordCommand.execute(request));
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Updated Successfully!");

        JSONObject user = getUserRequest().getJSONObject("data");
        assertTrue(Auth.verifyHash(newPassword, user.getString("password")));
    }

    @Test
    public void e_updatePasswordBack() {

        JSONObject body = new JSONObject();

        body.put("username", username);
        body.put("newPassword", password);
        body.put("oldPassword", "123456");
        JSONObject request = makeRequest(body, "PUT", new JSONObject());
        UpdatePassword updatePasswordCommand = new UpdatePassword();

        JSONObject response = new JSONObject(updatePasswordCommand.execute(request));
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Updated Successfully!");
    }

    @Test
    public void f_updateProfilePicture() {
        if (!Utilities.isDevelopmentMode())
            return;
        String photoUrl = "https://picsum.photos/200";
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("photoUrl", photoUrl);

        JSONObject request = makeRequest(body, "PUT", new JSONObject());
        UpdateProfilePhoto updateProfilePhotoCommand = new UpdateProfilePhoto();

        JSONObject response = new JSONObject(updateProfilePhotoCommand.execute(request));
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"), "Profile Picture uploaded successfully");


    }

    @Test
    public void g_deleteProfilePicture() {
        if (!Utilities.isDevelopmentMode())
            return;
        JSONObject body = new JSONObject();
        body.put("username", username);
        JSONObject request = makeRequest(body, "DELETE", new JSONObject());
        DeleteProfilePhoto deleteProfilePhotoCommand = new DeleteProfilePhoto();

        JSONObject response = new JSONObject(deleteProfilePhotoCommand.execute(request));
        JSONObject user = getUserRequest().getJSONObject("data");
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"), "Profile Picture deleted successfully");
        assertFalse(user.has("photo_url"));
    }

    @Test
    public void h_deleteAccount() {

        JSONObject body = new JSONObject();

        body.put("username", username);
        body.put("password", password);
        JSONObject request = makeRequest(body, "DELETE", new JSONObject());
        DeleteAccount deleteAccountCommand = new DeleteAccount();

        JSONObject response = new JSONObject(deleteAccountCommand.execute(request));
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Deleted Successfully!");

        JSONObject getUserResponse = getUserRequest();
        assertEquals(getUserResponse.getString("msg"), "User not found!");
    }


}
