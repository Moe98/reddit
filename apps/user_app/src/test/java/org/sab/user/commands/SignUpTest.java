package org.sab.user.commands;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.user.UserApp;

import java.sql.Connection;
import java.util.Date;
import java.util.UUID;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
    public void SignUpCreatesAnEntryInDB() {

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

    @After
    public void delete() {

        JSONObject body = new JSONObject();
        JSONObject request = new JSONObject();
        String username = createdUsername;
        String password = "to_the_moon";

        body.put("username", username);
        body.put("password", password);

        request.put("body", body);
        request.put("uriParams", new JSONObject());
        request.put("methodType", "DELETE");
        DeleteAccount deleteAccountCommand = new DeleteAccount();

        JSONObject response = new JSONObject(deleteAccountCommand.execute(request));
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Deleted Successfully!");


    }

}
