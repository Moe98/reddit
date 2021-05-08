package org.sab.user.commands;

import org.sab.models.User;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.SQLException;
import java.util.List;

public class GetUser extends UserCommand {
    @Override
    protected String execute() {
        if (!uriParams.has(USERNAME))
            return Responder.makeErrorResponse("You must add username in URIParams!", 400);
        String username = uriParams.getString(USERNAME);
        try {
            User user = getUser(username, USERNAME, PASSWORD, EMAIL, BIRTHDATE, PHOTO_URL, USER_ID);
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
