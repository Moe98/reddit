package org.sab.user.commands;

import org.sab.functions.CloudUtilities;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ChooseProfilePhoto extends UserCommand {
    @Override
    protected Schema getSchema() {
        Attribute username = new Attribute(USERNAME, DataType.STRING, true);
        Attribute photoUrl = new Attribute(PHOTO_URL, DataType.STRING);
        return new Schema(List.of(username, photoUrl));
    }

    @Override
    protected String execute() {
        // retrieving the body objects
        String username = body.getString(USERNAME);
        String photoUrl = "";
        if(body.keySet().contains(PHOTO_URL) && body.getString(PHOTO_URL).length() >0){
            photoUrl = body.getString(PHOTO_URL);
            try {
                photoUrl = CloudUtilities.uploadImage(photoUrl, username);
            } catch (IOException e) {
                return makeErrorResponse("An error occurred while uploading your image!", 400).toString();
            }
        }
        else{
            try {
                CloudUtilities.destroyImage(username);
            } catch (IOException e) {
                return makeErrorResponse("An error occurred while updating your image!", 400).toString();
            }
        }
        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("update_profile_picture", username, photoUrl);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return makeErrorResponse(e.getMessage(), 404).toString();
        }

        //retrieving the result from SQL into a User Object
        User user;
        try {
            ResultSet resultSet = PostgresConnection.call("get_user", username);

            if (resultSet == null || !resultSet.next()) {
                return makeErrorResponse("User not found!", 404).toString();
            }

            user = new User();
            user.setUserId(resultSet.getString("user_id"));
            user.setUsername(resultSet.getString(USERNAME));
            user.setEmail(resultSet.getString(EMAIL));
            user.setBirthdate(resultSet.getString(BIRTHDATE));
            user.setPhotoUrl(resultSet.getString(PHOTO_URL));
        } catch (PropertiesNotLoadedException | SQLException e) {
            return makeErrorResponse("An error occurred while submitting your request!", 502).toString();
        }

        return makeDataResponse(user.toJSON()).toString();
    }

    @Override
    protected String verifyBody() {
        String verifyBody = verifyBody(schema);
        return verifyBody;
    }
}
