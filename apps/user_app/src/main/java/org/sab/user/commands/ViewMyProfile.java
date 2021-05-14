package org.sab.user.commands;

import org.sab.models.user.User;
import org.sab.models.user.UserAttributes;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.SQLException;

public class ViewMyProfile extends UserCommand {
    @Override
    protected String execute() {
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
        return Schema.emptySchema();
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.GET;
    }

    @Override
    protected boolean isAuthNeeded() {
        return true;
    }
}
