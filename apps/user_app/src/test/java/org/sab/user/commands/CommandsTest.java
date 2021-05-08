package org.sab.user.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.arango.Arango;
import org.sab.functions.Auth;
import org.sab.functions.Utilities;
import org.sab.user.UserApp;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommandsTest {

    static String username = "scale-a-bull" + new Date().getTime();
    static String password = "12345678";
    static String token;


    @BeforeClass
    public static void connectToDbs() {
        try {
            UserApp.dbInit(true);
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }


    @Test
    public void T01_SignUpCreatesAnEntryInDB() {


        String email = username + "@gmail.com";
        String birthdate = "1997-12-14";


        JSONObject response = Requester.signUp(username, email, password, birthdate);
        System.out.println(response);

        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(data.getString("username"), username);
        assertEquals(data.getString("birthdate"), birthdate);
        assertEquals(data.getString("email"), email);

    }

    @Test
    public void T02_SignUpSameUser() {


        String email = username + "@gmail.com";
        String birthdate = "1997-12-14";


        JSONObject response = Requester.signUp(username, email, password, birthdate);
        System.out.println(response);
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode >= 500);

        String msg = response.getString("msg");
        assertTrue(msg.contains("duplicate key"));

    }

    @Test
    public void T03_SignUpMissingParam() {


        String email = username + "@gmail.com";
        String birthdate = null;


        JSONObject response = Requester.signUp(username, email, password, birthdate);
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode >= 400 && statusCode <= 500);

        String msg = response.getString("msg");
        assertTrue(msg.contains("missing") && msg.contains("birthdate"));

    }

    @Test
    public void T04_SignUpIncorrectlyFormattedDate() {


        String email = username + "@gmail.com";
        String birthdate = "14-12-1997";


        JSONObject response = Requester.signUp(username, email, password, birthdate);
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode >= 400 && statusCode <= 500);

        String msg = response.getString("msg");
        System.out.println(msg);
        assertTrue(msg.contains("birthdate must be of type SQL_DATE"));

    }

    @Test
    public void T05_Login() {


        JSONObject response = Requester.login(username, password);
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"), "Login Successful!");
        assertTrue(response.has("token"));
        token = response.getString("token");
        Requester.decodeToken(token);
    }

    @Test
    public void T06_GetUser() {

        JSONObject response = Requester.getUser(username);
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(data.getString("username"), username);
        assertTrue(Auth.verifyHash(password, data.getString("password")));

    }

    @Test
    public void T07_GetUserWithoutURIParams() {

        JSONObject response = Requester.getUser(null);
        System.out.println(response);
        int statusCode = response.getInt("statusCode");
        String msg = response.getString("msg");
        assertTrue(statusCode >= 400 && statusCode < 500);
        assertTrue(msg.equals("You must add username in URIParams!"));
    }

    @Test
    public void T08_updatePassword() {

        String newPassword = "123456";

        JSONObject response = Requester.updatePassword(password, newPassword);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Updated Successfully!");

        JSONObject user = Requester.getUser(username).getJSONObject("data");
        assertTrue(Auth.verifyHash(newPassword, user.getString("password")));
    }

    @Test
    public void T09_updatePasswordBack() {


        JSONObject response = Requester.updatePassword("123456", password);
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"), "Account Updated Successfully!");
    }

    @Test
    public void T10_newPasswordCannotMatchPreviousPassword() {


        JSONObject response = Requester.updatePassword(password, password);
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode >= 400);
        System.out.println(response);
        assertTrue(response.getString("msg").contains("cannot match"));
    }

    @Test
    public void T11_updateProfilePicture() {
        if (!Utilities.isDevelopmentMode())
            return;
        String photoUrl = "https://picsum.photos/200";
        JSONObject response = Requester.updateProfilePicture(photoUrl);
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"), "Profile Picture uploaded successfully");


    }

    @Test
    public void T12_deleteProfilePicture() {
        if (!Utilities.isDevelopmentMode())
            return;

        JSONObject response = Requester.deleteProfilePicture();
        JSONObject user = Requester.getUser(username).getJSONObject("data");
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(response.getString("msg"), "Profile Picture deleted successfully");
        assertFalse(user.has("photo_url"));
    }


    @Test
    public void T13_deleteAccount() {


        JSONObject response = Requester.deleteAccount(password);
        System.out.println(response);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals(response.getString("msg"), "Account Deleted Successfully!");

        JSONObject getUserResponse = Requester.getUser(username);
        assertEquals(getUserResponse.getString("msg"), "User not found!");
    }

    @AfterClass
    public static void deleteFromArano() {
        Arango arango = Arango.getInstance();
        ArangoDB arangoDB = arango.connect();
        String dbName = System.getenv("ARANGO_DB");
        BaseDocument user = arango.readDocument(arangoDB, dbName, "Users", username);
        Map props = user.getProperties();
        boolean is_deleted = (boolean) props.get("is_deleted");
        assertTrue(is_deleted);
        arango.deleteDocument(arangoDB, dbName, "Users", username);
    }
}
