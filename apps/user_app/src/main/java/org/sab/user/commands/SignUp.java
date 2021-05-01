package org.sab.user.commands;

import org.sab.functions.Auth;
import org.sab.validation.DataType;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.validation.Schema;
import org.sab.validation.Attribute;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;


public class SignUp extends UserCommand {

    @Override
    protected Schema getSchema() {
        Attribute username = new Attribute(USERNAME, DataType.STRING, true);
        Attribute email = new Attribute(EMAIL, DataType.EMAIL, true);
        Attribute password = new Attribute(PASSWORD, DataType.STRING, true);
        Attribute birthdate = new Attribute(BIRTHDATE, DataType.SQL_DATE, true);
        Attribute photoUrl = new Attribute(PHOTO_URL, DataType.STRING);

        return new Schema(List.of(username, email, password, birthdate, photoUrl));
    }

    @Override
    protected String execute() {

        // retrieving the body objects
        String username = body.getString(USERNAME);
        String userId = UUID.randomUUID().toString();
        String hashedPassword;
        try {
            hashedPassword = Auth.encrypt(body.getString(PASSWORD));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return makeErrorResponse(e.getMessage(), 404).toString();
        }
        String email = body.getString(EMAIL);
        String photoUrl = body.keySet().contains(PHOTO_URL) ? body.getString(PHOTO_URL) : null;
        Date birthdate = Date.valueOf(body.getString(BIRTHDATE));

        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("create_user", userId, username, email, hashedPassword, birthdate, photoUrl);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return makeErrorResponse(e.getMessage(), 404).toString();
        }

        //retrieving the result from SQL into a User Object
        User user;
        try {
            ResultSet resultSet = PostgresConnection.call("get_user", username);

            if (resultSet == null || !resultSet.next()) {
                return makeErrorResponse("ResultSet is Empty!", 404).toString();
            }

            user = new User();

            user.setUserId(resultSet.getString("user_id"));
            user.setUsername(resultSet.getString(USERNAME));
            user.setEmail(resultSet.getString(EMAIL));
            user.setPassword(resultSet.getString(PASSWORD));
            user.setBirthdate(resultSet.getString(BIRTHDATE));
            user.setPhotoUrl(resultSet.getString("photo_url"));

        } catch (PropertiesNotLoadedException | SQLException e) {
            return makeErrorResponse("ResultSet is Empty!", 404).toString();
        }

        return makeDataResponse(user.toJSON()).toString();
    }

}
