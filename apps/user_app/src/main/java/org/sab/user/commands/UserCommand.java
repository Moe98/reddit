package org.sab.user.commands;


import org.json.JSONObject;
import org.sab.auth.Auth;
import org.sab.models.user.User;
import org.sab.models.user.UserAttributes;
import org.sab.postgres.PostgresConnection;
import org.sab.service.validation.CommandWithVerification;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class UserCommand extends CommandWithVerification {
    protected static final String USERNAME = UserAttributes.USERNAME.toString();
    protected static final String EMAIL = UserAttributes.EMAIL.toString();
    protected static final String PASSWORD = UserAttributes.PASSWORD.toString();
    protected static final String BIRTHDATE = UserAttributes.BIRTHDATE.toString();
    protected static final String PHOTO_URL = UserAttributes.PHOTO_URL.toString();
    protected static final String USER_ID = UserAttributes.USER_ID.toString();
    protected static final String NEW_PASSWORD = "newPassword";
    protected static final String OLD_PASSWORD = "oldPassword";
    protected static final String BUCKETNAME = "profile-picture-scaleabull";

    protected JSONObject authenticateUser(String username, String password) {
        boolean checkPassword;
        User user;
        try {
            user = getUser(username, UserAttributes.PASSWORD, UserAttributes.USER_ID, UserAttributes.PHOTO_URL);
            String hashedPassword = user.getPassword();
            checkPassword = Auth.verifyHash(password, hashedPassword);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return new JSONObject().put("msg", e.getMessage()).put("statusCode", 502);
        }
        if (!checkPassword) {
            return new JSONObject().put("msg", "Password is incorrect").put("statusCode", 401);
        }
        return new JSONObject().put("msg", "User Authentication successful!").put("statusCode", 200).put("data", user.toJSON());
    }

    protected User getUser(String username, UserAttributes... userAttributes) throws SQLException, EnvironmentVariableNotLoaded {
        ResultSet resultSet = PostgresConnection.call("get_user", username);
        if (resultSet == null || !resultSet.next()) {
            throw new SQLException("User not found!");
        }

        User user = new User();
        for (UserAttributes userAttribute : userAttributes)
            userAttribute.setAttribute(user, resultSet);
        return user;
    }


}
