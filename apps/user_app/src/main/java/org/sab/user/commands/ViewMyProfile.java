package org.sab.user.commands;

import org.sab.models.user.User;
import org.sab.models.user.UserAttributes;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.SQLException;
import java.util.List;

public class ViewMyProfile extends UserCommand {
    @Override
    protected String execute() {

        boolean authenticated = authenticationParams.getBoolean(IS_AUTHENTICATED);
        if (!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 403);

        String username = authenticationParams.getString(USERNAME);
        try {
            User user = getUser(username, UserAttributes.USERNAME, UserAttributes.PASSWORD, UserAttributes.EMAIL, UserAttributes.BIRTHDATE, UserAttributes.PHOTO_URL, UserAttributes.USER_ID);
            return Responder.makeDataResponse(user.toJSON());
        } catch (SQLException | EnvironmentVariableNotLoaded e) {
            return Responder.makeErrorResponse(e.getMessage(), 500);
        }
    }

    @Override
    protected Schema getSchema() {
        return new Schema(List.of());
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }
}
