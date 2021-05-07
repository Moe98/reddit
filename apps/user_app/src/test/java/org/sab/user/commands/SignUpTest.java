package org.sab.user.commands;

import org.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.sab.functions.Auth;
import org.sab.user.UserApp;

import java.util.*;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SignUpTest {

    static String username = "scale-a-bull" + new Date().getTime();
    static String password = "12345678";

    JSONObject makeRequest(JSONObject body, String methodType, JSONObject uriParams) {
        JSONObject request = new JSONObject();
        request.put("body", body);
        request.put("methodType", methodType);
        request.put("uriParams", uriParams);
        return request;
    }

    @BeforeClass
    public static void connectToSql() {
        try {
            UserApp.dbInit();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    public JSONObject makeGETRequest(String username) {
        JSONObject uriParams = new JSONObject().put("username", username);
        JSONObject request = makeRequest(new JSONObject(), "GET", uriParams);
        return request;
    }
    public JSONObject getUserRequest(){
        JSONObject request = makeGETRequest(username);
        GetUser getUserCommand = new GetUser();
        JSONObject response = new JSONObject(getUserCommand.execute(request));
        return response;
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
        body.put("userId", UUID.randomUUID().toString());

        JSONObject request = makeRequest(body, "POST", new JSONObject());
        SignUp signUpCommand = new SignUp();
        JSONObject response = new JSONObject(signUpCommand.execute(request));
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = null;
        try {
            data = response.getJSONObject("data");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(data.getString("username"), username);
        assertEquals(data.getString("birthdate"), birthdate);


    }

    @Test
    public void b_GetUser() {

        JSONObject response = getUserRequest();
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = null;
        try {
            data = response.getJSONObject("data");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(data.getString("username"), username);
        assertTrue(Auth.verifyHash(password,data.getString("password")));


    }

    @Test
    public void c_updatePassword() {

        JSONObject body = new JSONObject();

        body.put("username", username);
        body.put("newPassword", "123456");
        body.put("oldPassword", password);
        JSONObject request = makeRequest(body, "PUT", new JSONObject());
        UpdatePassword updatePasswordCommand = new UpdatePassword();

        JSONObject response = new JSONObject(updatePasswordCommand.execute(request));
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Updated Successfully!");
    }
    @Test
    public void d_updatePasswordBack() {

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
    public void e_updateProfilePicture() {
        //ENV_TYPE: Development OR Production
        String photoUrl = "https://picsum.photos/200";
        String mode = System.getenv("ENV_TYPE");
        System.out.println(mode);
        if (mode == null || !mode.equals("Development"))
            return;
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
    public void f_deleteProfilePicture() {
        String mode = System.getenv("ENV_TYPE");
        if (mode == null || !mode.equals("Development"))
            return;
        JSONObject body = new JSONObject();
        body.put("username", username);
        JSONObject request = makeRequest(body, "DELETE", new JSONObject());
        DeleteProfilePhoto deleteProfilePhotoCommand = new DeleteProfilePhoto();

        JSONObject response = new JSONObject(deleteProfilePhotoCommand.execute(request));
        JSONObject userData = getUserRequest();
        JSONObject data = null;
        try {
            data = userData.getJSONObject("data");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"), "Profile Picture deleted successfully");
        assertFalse(data.has("photo_url"));
    }

    @Test
    public void g_delete() {

        JSONObject body = new JSONObject();

        body.put("username", username);
        body.put("password", password);
        JSONObject request = makeRequest(body, "DELETE", new JSONObject());
        DeleteAccount deleteAccountCommand = new DeleteAccount();

        JSONObject response = new JSONObject(deleteAccountCommand.execute(request));
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Deleted Successfully!");
    }

}
