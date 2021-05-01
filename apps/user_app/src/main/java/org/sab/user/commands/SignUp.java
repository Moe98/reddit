package org.sab.user.commands;

import org.sab.functions.Auth;
import org.sab.validation.DataType;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class SignUp extends UserCommand {


    static void initSchema() {
        params = new String[]{"username", "email", "password", "birthdate", "photoUrl"};
        dataTypes = new DataType[]{DataType.String, DataType.Email, DataType.String, DataType.SQLDate, DataType.String};
        isRequired = new boolean[]{true, true, true, true, false};
    }

    static {
        initSchema();
    }


    static String[] params;
    static DataType[] dataTypes;
    static boolean[] isRequired;


    @Override
    protected String execute() {

        // retrieving the body objects
        String username = body.getString("username");
        String userId = UUID.randomUUID().toString();
        String hashedPassword;
        try {
            hashedPassword = Auth.encrypt(body.getString("password"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return sendError(e.getMessage(), 404).toString();
        }
        String email = body.getString("email");
        String photoUrl = body.keySet().contains("photo_url") ? body.getString("photo_url") : null;
        Date birthdate = Date.valueOf(body.getString("birthdate"));

        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("create_user", new Object[]{userId, username, email, hashedPassword, birthdate, photoUrl});
        } catch (PropertiesNotLoadedException | SQLException e) {
            return sendError(e.getMessage(), 404).toString();
        }

        //retrieving the result from SQL into a User Object
        User user;
        try {
            ResultSet resultSet = PostgresConnection.call("get_user", new Object[]{username});

            if (resultSet == null || !resultSet.next()) {
                return sendError("ResultSet is Empty!", 404).toString();
            }

            user = new User();

            user.setUserId(resultSet.getString("user_id"));
            user.setUsername(resultSet.getString("username"));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("password"));
            user.setBirthdate(resultSet.getString("birthdate"));
            user.setPhotoUrl(resultSet.getString("photo_url"));

        } catch (PropertiesNotLoadedException | SQLException e) {
            return UserCommand.sendError("ResultSet is Empty!", 404).toString();
        }

        return sendData(user.toJSON()).toString();
    }

    @Override
    protected String verifyBody() {
        String verifyBody = verifyBody(params, dataTypes, isRequired);
        return verifyBody;
    }


}
