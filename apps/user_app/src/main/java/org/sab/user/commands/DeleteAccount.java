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

public class DeleteAccount extends UserCommand {


    protected Schema getSchema() {

        Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        Attribute password = new Attribute(PASSWORD, DataType.PASSWORD, true);

        return new Schema(List.of(username, password));
    }

    @Override
    protected String execute() {
        // retrieving the body objects

        String username = body.getString(USERNAME);
        String password = body.getString(PASSWORD);

        // Authentication
        JSONObject userAuth = authenticateUser(username, password);
        if (userAuth.getInt("statusCode") != 200)
            return userAuth.toString();

        //calling the delete SQL procedure
        try {
            PostgresConnection.call("delete_user", username);
            deleteProfilePicture(username);
            deleteFromArango(username);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }


        return Responder.makeMsgResponse("Account Deleted Successfully!");
    }

    private void deleteProfilePicture(String username) {
        //TODO
    }

    private void deleteFromArango(String username) {
        //TODO
    }
}
