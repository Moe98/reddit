package org.sab.user.commands;

import org.sab.functions.CloudUtilities;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.IOException;
import java.sql.SQLException;

public class DeleteProfilePhoto extends UserCommand {
    @Override
    protected Schema getSchema() {
        return null;
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.DELETE;
    }

    @Override
    protected String execute() {
        Boolean authenticated = authenticationParams.getBoolean(Authenticated);
        if(!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);

        // retrieving the body objects
        String username = authenticationParams.getString(USERNAME);

        // getting the user
        try {
            User user = getUser(username, PHOTO_URL);
            if (user.getPhotoUrl() == null)
                return Responder.makeMsgResponse("You don't have a profile picture, you can't delete your avatar!");

        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

        // Deleting from Cloudinary
        try {
            CloudUtilities.destroyImage(username);
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
