package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.auth.Auth;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.SQLException;
import java.util.List;

public class UpdatePassword extends UserCommand {


    protected Schema getSchema() {
        Attribute oldPassword = new Attribute(OLD_PASSWORD, DataType.PASSWORD, true);
        Attribute newPassword = new Attribute(NEW_PASSWORD, DataType.PASSWORD, true);
        return new Schema(List.of(oldPassword, newPassword));
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

        String username = authenticationParams.getString(USERNAME);
        String oldPassword = body.getString(OLD_PASSWORD);
        String newPassword = body.getString(NEW_PASSWORD);

        if (oldPassword.equals(newPassword))
            return Responder.makeErrorResponse("Your new password cannot match your last one.", 400);

        JSONObject userAuth = authenticateUser(username, oldPassword);
        if (userAuth.getInt("statusCode") != 200)
            return userAuth.toString();

        try {
            newPassword = Auth.hash(newPassword);
            PostgresConnection.call("update_user_password", username, newPassword);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }


        return Responder.makeMsgResponse("Account Updated Successfully!");
    }

}
