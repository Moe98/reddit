package org.sab.user.commands;

import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sab.arango.Arango;
import org.sab.auth.Auth;
import org.sab.functions.Utilities;
import org.sab.models.user.User;
import org.sab.models.user.UserAttributes;
import org.sab.user.UserApp;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserAppTest {

    static String username = "scale-a-bull" + new Date().getTime();
    static String password = "12345678";
    static String token;


    @BeforeClass
    public static void connectToDbs() {
        try {
            UserApp.dbInit();
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }


    @Test
    public void T01_SignUpCreatesAnEntryInDB() {


        String email = username + "@gmail.com";
        String birthdate = "1997-12-14";


        JSONObject response = Requester.signUp(username, email, password, birthdate);

        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(username, data.getString("username"));
        assertEquals(birthdate, data.getString("birthdate"));
        assertEquals(email, data.getString("email"));

    }

    @Test
    public void T02_SignUpSameUser() {


        String email = username + "@gmail.com";
        String birthdate = "1997-12-14";


        JSONObject response = Requester.signUp(username, email, password, birthdate);
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
        assertTrue(msg.contains("birthdate must be of type SQL_DATE"));

    }

    @Test
    public void T05_Login() {


        JSONObject response = Requester.login(username, password);
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("Login Successful!", response.getString("msg"));
        assertTrue(response.has("token"));
        token = response.getString("token");
        Requester.decodeToken(token);
    }

    @Test
    public void T06_GetUser() {

        JSONObject response = Requester.getUser(username);
        assertEquals(200, response.getInt("statusCode"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(username, data.getString("username"));
        assertTrue(Auth.verifyHash(password, data.getString("password")));

    }

    @Test
    public void T07_GetUserWithoutURIParams() {

        JSONObject response = Requester.getUser(null);
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

        assertEquals("Account Updated Successfully!", response.getString("msg"));

        JSONObject user = Requester.getUser(username).getJSONObject("data");
        assertTrue(Auth.verifyHash(newPassword, user.getString("password")));
    }

    @Test
    public void T09_updatePasswordBack() {


        JSONObject response = Requester.updatePassword("123456", password);
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("Account Updated Successfully!", response.getString("msg"));
    }

    @Test
    public void T10_newPasswordCannotMatchPreviousPassword() {


        JSONObject response = Requester.updatePassword(password, password);
        int statusCode = response.getInt("statusCode");
        assertTrue(statusCode >= 400);
        assertTrue(response.getString("msg").contains("cannot match"));
    }

    @Test
    public void T11_updateProfilePicture() {
        if (!Utilities.isDevelopmentMode())
            return;
        String photoUrl = "https://picsum.photos/200";
        JSONObject response = Requester.updateProfilePicture(photoUrl);
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("Profile Picture uploaded successfully", response.getString("msg"));


    }

    @Test
    public void T12_viewAnotherProfile() {

        JSONObject response = Requester.viewAnotherProfile(username);
        JSONObject user = Requester.getUser(username).getJSONObject("data");
        assertEquals(200, response.getInt("statusCode"));
        assertEquals(username, user.getString("username"));
        assertTrue(!Utilities.isDevelopmentMode() || user.has("photoUrl"));
    }

    @Test
    public void T13_deleteProfilePicture() {
        if (!Utilities.isDevelopmentMode())
            return;

        JSONObject response = Requester.deleteProfilePicture();
        JSONObject user = Requester.getUser(username).getJSONObject("data");
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("Profile Picture deleted successfully", response.getString("msg"));
        assertFalse(user.has("photoUrl"));
    }


    @Test
    public void T14_deleteAccount() {


        JSONObject response = Requester.deleteAccount(password);
        assertEquals(200, response.getInt("statusCode"));

        assertEquals("Account Deleted Successfully!", response.getString("msg"));

        JSONObject getUserResponse = Requester.getUser(username);
        assertEquals("User not found!", getUserResponse.getString("msg"));
    }

    @AfterClass
    public static void deleteFromArango() {
        Arango arango = Arango.getInstance();
        arango.connectIfNotConnected();
        BaseDocument user = arango.readDocument(UserApp.ARANGO_DB_NAME, User.getCollectionName(), username);
        Map props = user.getProperties();
        boolean isDeleted = (boolean) props.get(UserAttributes.IS_DELETED.getArangoDb());
        assertTrue(isDeleted);
        int numberOfFollowers = (int) props.get(UserAttributes.NUM_OF_FOLLOWERS.getArangoDb());
        assertEquals(0, numberOfFollowers);
        arango.deleteDocument(UserApp.ARANGO_DB_NAME, User.getCollectionName(), username);
    }
}
