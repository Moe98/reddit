package org.sab.user.commands;


import org.json.JSONObject;
import org.sab.functions.Auth;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
import org.sab.service.validation.CommandWithVerification;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class UserCommand extends CommandWithVerification {
    protected static final String USERNAME = "username";
    protected static final String EMAIL = "email";
    protected static final String PASSWORD = "password";
    protected static final String BIRTHDATE = "birthdate";
    protected static final String PHOTO_URL = "photoUrl";
    protected static final String NEW_PASSWORD = "newPassword";
    protected static final String USER_ID = "userId";
    protected static final String OLD_PASSWORD = "oldPassword";
    protected static final String Authenticated = "isAuthenticated";

    protected JSONObject authenticateUser(String username, String password) {
        boolean checkPassword;

        try {
            User user = getUser(username, PASSWORD);
            String hashedPassword = user.getPassword();
            checkPassword = Auth.verifyHash(password, hashedPassword);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return new JSONObject().put("msg", e.getMessage()).put("statusCode", 502);
        }
        if (!checkPassword) {
            return new JSONObject().put("msg", "Password is incorrect").put("statusCode", 401);
        }
        return new JSONObject().put("msg", "User Authentication successful!").put("statusCode", 200);
    }

    protected User getUser(String username, String... userAttributes) throws SQLException, EnvironmentVariableNotLoaded {
        ResultSet resultSet = PostgresConnection.call("get_user", username);
        if (resultSet == null || !resultSet.next()) {
            throw new SQLException("User not found!");
        }

        User user = new User();
        for (String attribute : userAttributes)
            switch (attribute) {
                case PASSWORD:
                    user.setPassword(resultSet.getString(PASSWORD));
                case USERNAME:
                    user.setUsername(resultSet.getString(USERNAME));
                case USER_ID:
                    user.setUserId(resultSet.getString("user_id"));
                case BIRTHDATE:
                    user.setBirthdate(resultSet.getString(BIRTHDATE));
                case EMAIL:
                    user.setEmail(resultSet.getString(EMAIL));
                case PHOTO_URL:
                    user.setPhotoUrl(resultSet.getString("photo_url"));
            }


        return user;
    }


}
