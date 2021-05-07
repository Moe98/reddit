package org.sab.user.commands;

import org.sab.functions.CloudUtilities;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DeleteProfilePhoto extends UserCommand {
    @Override
    protected Schema getSchema() {
        Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        return new Schema(List.of(username));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.DELETE;
    }

    @Override
    protected String execute() {
        // retrieving the body objects
        String username = body.getString(USERNAME);

        // getting the user
        try {
            User user = getUser(username, PHOTO_URL);
            if (user.getPhotoUrl() == null)
                return Responder.makeMsgResponse("You don't have a profile picture, you can't delete your avatar!");

        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

        // Deleting from Cloudinary
        try {
            CloudUtilities.destroyImage(username);
        } catch (IOException e) {
            return Responder.makeErrorResponse("An error occurred while updating your image!", 400);
        }

        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("delete_profile_picture", username);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }

        return Responder.makeMsgResponse("Profile Picture deleted successfully");
    }
}
