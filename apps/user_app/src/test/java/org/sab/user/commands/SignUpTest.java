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

    static String createdUsername;

    @BeforeClass
    public static void connectToSql() {
        try {
            UserApp.dbInit();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }


    @Test
    public void a_SignUpCreatesAnEntryInDB() {

        JSONObject body = new JSONObject();
        long time = new Date().getTime();
        JSONObject request = new JSONObject();
        createdUsername = "scaleabull" + time;
        String username = createdUsername;
        String password = "to_the_moon";
        String email = "scaleabul" + time + "@gmail.com";
        String birthdate = "1997-12-14";

        body.put("username", username);
        body.put("password", password);
        body.put("email", email);
        body.put("birthdate", birthdate);
        body.put("userId", UUID.randomUUID().toString());

        request.put("body", body);
        request.put("uriParams", new JSONObject());
        request.put("methodType", "POST");
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
        JSONObject body = new JSONObject();
        JSONObject request = new JSONObject();
        String username = createdUsername;
        request.put("body", body);
        Map<String, List<String>> uriParams = new HashMap<>();
        uriParams.put("username", List.of(username));
        request.put("uriParams", uriParams);
        request.put("methodType", "GET");
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
        assertTrue(Auth.verifyHash("to_the_moon", data.getString("password")));

    }

    @AfterClass
    public static void delete() {

        JSONObject body = new JSONObject();
        JSONObject request = new JSONObject();
        String username = createdUsername;
        String password = "to_the_moon";

        body.put("username", username);
        body.put("password", password);

        request.put("body", body);
        Map<String, List<String>> uriParams = new HashMap<>();
        request.put("uriParams", uriParams);
        request.put("methodType", "DELETE");
        DeleteAccount deleteAccountCommand = new DeleteAccount();

        JSONObject response = new JSONObject(deleteAccountCommand.execute(request));
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Deleted Successfully!");


    }

}
