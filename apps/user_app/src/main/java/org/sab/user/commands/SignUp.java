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
        String verifyBody = verifyBody(schema);
        return verifyBody;
    }


}
