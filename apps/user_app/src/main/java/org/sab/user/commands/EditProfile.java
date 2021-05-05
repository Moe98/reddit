package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.functions.Auth;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EditProfile extends UserCommand {


    protected Schema getSchema() {
        Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        Attribute oldPassword = new Attribute(OLD_PASSWORD, DataType.PASSWORD, true);
        Attribute newPassword = new Attribute(NEW_PASSWORD, DataType.PASSWORD, true);
        return new Schema(List.of(username, oldPassword, newPassword));
    }

    @Override
    protected String execute() {

        // retrieving the body objects
        String username = body.getString(USERNAME);
        String oldPassword = body.getString(OLD_PASSWORD);
        String newPassword = body.getString(NEW_PASSWORD);

        if (oldPassword.equals(newPassword))
            return Responder.makeErrorResponse("Your new password cannot match your last one.", 400);
        // Authentication
        JSONObject userAuth = authenticateUser(username, oldPassword);
        if (userAuth.getInt("statusCode") != 200)
            return userAuth.toString();

        //calling the appropriate SQL procedure
        try {
            newPassword = Auth.hash(newPassword);
            PostgresConnection.call("update_user_password", username, newPassword);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404);
        }


        return Responder.makeMsgResponse("Account Updated Successfully!");
    }

}
