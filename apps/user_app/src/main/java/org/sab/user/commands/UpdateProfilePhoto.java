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

public class UpdateProfilePhoto extends UserCommand {
    @Override
    protected Schema getSchema() {
        return Schema.emptySchema();

    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.PUT;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }

    @Override
    protected String execute() {
        if (files.length() != 1)
            return Responder.makeErrorResponse("Only one profile image allowed per upload, Check Form-Data Files!", 400);

        String username = authenticationParams.getString(USERNAME);
        String photoUrl;
        User user;
        try {
            user = getUser(username, UserAttributes.USER_ID);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

        try {
            photoUrl = MinIO.uploadObject(BUCKETNAME, user.reformatUserId(), files.getJSONObject("image"));
            if (photoUrl.isEmpty())
                return Responder.makeErrorResponse("Error Occurred While Uploading Your Image!", 404);
        } catch (EnvironmentVariableNotLoaded e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        }


        try {
            PostgresConnection.call("update_profile_picture", username, photoUrl);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }

        return Responder.makeMsgResponse(String.format("Profile Picture uploaded successfully. You can find at %s", photoUrl));
    }
}
