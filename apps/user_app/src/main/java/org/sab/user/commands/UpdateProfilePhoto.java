package org.sab.user.commands;

import org.sab.functions.CloudUtilities;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UpdateProfilePhoto extends UserCommand {
    @Override
    protected Schema getSchema() {
        Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        Attribute photoUrl = new Attribute(PHOTO_URL, DataType.STRING, true);
        return new Schema(List.of(username, photoUrl));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {
        Boolean authenticated = authenticationParams.getBoolean(Authenticated);
        if(!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);

        // retrieving the body objects
        String username = body.getString(USERNAME);
        String photoUrl = body.getString(PHOTO_URL);

        // getting the user
        try {
            getUser(username);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

        try {
            photoUrl = CloudUtilities.uploadImage(photoUrl, username);
        } catch (IOException | EnvironmentVariableNotLoaded e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        } catch (Exception e) {
            return Responder.makeErrorResponse("An error occurred while uploading your image!", 400);
        }


        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("update_profile_picture", username, photoUrl);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeMsgResponse("Profile Picture uploaded successfully");
    }
}
