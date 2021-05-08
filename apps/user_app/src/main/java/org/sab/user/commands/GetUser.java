package org.sab.user.commands;

import org.sab.models.User;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Schema;

import java.sql.SQLException;
import java.util.List;

public class GetUser extends UserCommand {
    @Override
    protected String execute() {
        Boolean authenticated = authenticationParams.getBoolean(Authenticated);
        if(!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);

        if (!uriParams.keySet().contains(USERNAME))
            return Responder.makeErrorResponse("You must add username in URIParams!", 400);
        String username = uriParams.getString(USERNAME);
        try {
            User user = getUser(username, USERNAME, PASSWORD, EMAIL, BIRTHDATE, PHOTO_URL, USER_ID);
            return Responder.makeDataResponse(user.toJSON());
        } catch (SQLException | PropertiesNotLoadedException e) {
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
