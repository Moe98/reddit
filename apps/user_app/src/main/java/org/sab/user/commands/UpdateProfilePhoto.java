package org.sab.user.commands;

import org.sab.minio.MinIO;
import org.sab.models.user.User;
import org.sab.models.user.UserAttributes;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.SQLException;
import java.util.List;

public class UpdateProfilePhoto extends UserCommand {
    @Override
    protected Schema getSchema() {
        return new Schema(List.of());

    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected String execute() {
        boolean authenticated = authenticationParams.getBoolean(IS_AUTHENTICATED);
        if (!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);
        if (files.length() != 1)
            return Responder.makeErrorResponse("Only one profile image allowed per upload, Check Form-Data Files!", 400);

        // retrieving the body objects
        String username = authenticationParams.getString(USERNAME);
        String output;
        User user;
        // getting the user
        try {
            user = getUser(username, UserAttributes.USER_ID);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }
        String publicId = user.reformatUserId(user.getUserId());

        try {
            output = MinIO.uploadObject(BUCKETNAME, publicId, files.getJSONObject("image"));
            if (output.isEmpty())
                return Responder.makeErrorResponse("Error Occurred While Uploading Your Image!", 404);
        } catch (EnvironmentVariableNotLoaded e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        }


        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("update_profile_picture", username, output);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeMsgResponse("Profile Picture uploaded successfully");
    }
}
