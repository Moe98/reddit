package org.sab.user.commands;

import org.sab.cloudinary.CloudinaryUtilities;
import org.sab.models.user.User;
import org.sab.models.user.UserAttributes;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DeleteProfilePhoto extends UserCommand {
    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.DELETE;
    }

    @Override
    protected String execute() {
        boolean authenticated = authenticationParams.getBoolean(AUTHENTICATED);
        if (!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);


        String username = authenticationParams.getString(USERNAME);

        User user;
        // getting the user
        try {
            user = getUser(username, UserAttributes.PHOTO_URL, UserAttributes.USER_ID);
            if (user.getPhotoUrl() == null)
                return Responder.makeMsgResponse("You don't have a profile picture, you can't delete your avatar!");

        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

        // Deleting from Cloudinary
        try {
            CloudinaryUtilities.deleteImage(user.getUserId());
        } catch (IOException | EnvironmentVariableNotLoaded e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        }

        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("delete_profile_picture", username);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeMsgResponse("Profile Picture deleted successfully");
    }
}
