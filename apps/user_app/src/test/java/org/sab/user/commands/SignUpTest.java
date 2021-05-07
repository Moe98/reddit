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

        JSONObject request = makeGETRequest(username);
        GetUser getUserCommand = new GetUser();

        JSONObject response = new JSONObject(getUserCommand.execute(request));
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

        //assert message is correct

        JSONObject getRequest = makeGETRequest(username);
        //make get request and assert that the password has been updated
    }

    @Test
    public void d_updateProfilePicture() {
        //ENV_TYPE: Development OR Production
        String photoUrl = "https://picsum.photos/200";
        String mode = System.getenv("ENV_TYPE");
        if (mode == null || !mode.equals("Development"))
            return;

        //execute ProfilePictureUpdate
        //assert msg  is updated succseffuly


    }

    @Test
    public void e_deleteProfilePicture() {
        String mode = System.getenv("ENV_TYPE");
        if (mode == null || !mode.equals("Development"))
            return;

        //execute ProfilePictureDelete
        //assert msg  is deleted succeffuly

        JSONObject getRequest = makeGETRequest(username);
        //make get request and assert that the photoUrl is null
    }

    @Test
    public void f_delete() {

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
