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

    static Schema initSchema() {
        Attribute username = new Attribute("username", DataType.String, true);
        Attribute photoUrl = new Attribute("photoUrl", DataType.String);

        schema = new Schema(List.of(username,photoUrl));
        return schema;
    }

    static Schema schema = initSchema();

    @Override
    protected String execute() {
        // retrieving the body objects
        String username = body.getString("username");
        String photoUrl = "";
        if(body.keySet().contains("photo_url") && body.getString("photo_url").length() >0){
            photoUrl = body.getString("photo_url");
            try {
                photoUrl = CloudUtilities.uploadImage(photoUrl,username);
            } catch (IOException e) {
                return sendError("An error occurred while uploading your image!", 400).toString();
            }
        }
        else{
            try {
                CloudUtilities.destroyImage(username);
            } catch (IOException e) {
                return sendError("An error occurred while updating your image!", 400).toString();
            }
        }
        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("update_profile_picture", new Object[]{username, photoUrl});
        } catch (PropertiesNotLoadedException | SQLException e) {
            return sendError(e.getMessage(), 404).toString();
        }

        //retrieving the result from SQL into a User Object
        User user;
        try {
            ResultSet resultSet = PostgresConnection.call("get_user", new Object[]{username});

            if (resultSet == null || !resultSet.next()) {
                return sendError("User not found!", 404).toString();
            }

            user = new User();
            user.setUserId(resultSet.getString("user_id"));
            user.setUsername(resultSet.getString("username"));
            user.setEmail(resultSet.getString("email"));
            user.setBirthdate(resultSet.getString("birthdate"));
            user.setPhotoUrl(resultSet.getString("photo_url"));
        } catch (PropertiesNotLoadedException | SQLException e) {
            return sendError("An error occurred while submitting your request!", 502).toString();
        }

        return sendData(user.toJSON()).toString();
    }

    @Override
    protected String verifyBody() {
        String verifyBody = verifyBody(schema);
        return verifyBody;
    }
}
