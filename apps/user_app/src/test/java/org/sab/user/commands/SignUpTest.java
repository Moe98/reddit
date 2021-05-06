package org.sab.user.commands;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sab.postgres.PostgresConnection;

import java.sql.Connection;
import java.util.UUID;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SignUpTest {

    static Connection sqlConnection;

    @BeforeClass
    public static void connectToSql() {
        try {
            sqlConnection = PostgresConnection.getInstance().connect();
            PostgresConnection.dbInit();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }


    @Test
    public void SignUpCreatesAnEntryInDB() {

        JSONObject body = new JSONObject();

        JSONObject request = new JSONObject();

        String username = "scaleabull";
        String password = "to_the_moon";
        String email = "scaleabul@gmail.com";
        String birthdate = "1997-12-14";

        body.put("username", username);
        body.put("password", password);
        body.put("email", email);
        body.put("birthdate", birthdate);
        body.put("userId", UUID.randomUUID().toString());

        request.put("body", body);
        request.put("uriParams", new JSONObject());
        request.put("methodType","POST");
        SignUp signUpCommand = new SignUp();

        JSONObject response = new JSONObject(signUpCommand.execute(request));

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

//    @After
//    public void deleteTable() {
//        try {
//            PostgresConnection.deleteProcedures(sqlConnection);
//            PostgresConnection.deleteUsersTable(sqlConnection);
//        } catch (Exception e) {
//            fail(e.getMessage());
//        }
//
//
//    }

}
