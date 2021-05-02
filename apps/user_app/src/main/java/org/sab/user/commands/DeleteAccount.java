package org.sab.user.commands;

import org.sab.functions.Auth;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.user.Responder;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DeleteAccount extends UserCommand {


    protected Schema getSchema() {
        Attribute username = new Attribute(USERNAME, DataType.STRING, true);
        Attribute password = new Attribute(PASSWORD, DataType.STRING,true);
        return new Schema(List.of(username, password));
    }

    @Override
    protected String execute() {
        // retrieving the body objects
        String username = body.getString(USERNAME);
        String password = body.getString(PASSWORD);
        Boolean checkPassword = false;
        //retrieving the result from SQL into a User Object
        try {
            ResultSet resultSet = PostgresConnection.call("get_user", username);

            if (resultSet == null || !resultSet.next()) {
                return Responder.makeErrorResponse("User not found!", 404).toString();
            }

            String hashedPassword = resultSet.getString("password");
            checkPassword = Auth.verifyHash(password,hashedPassword);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse("An error occurred while submitting your request!", 502).toString();
        }
        if(!checkPassword){
            return Responder.makeErrorResponse("Error Occurred While Confirming Your Password", 401).toString();
        }
        //calling the appropriate SQL procedure
        try {
            PostgresConnection.call("delete_user", username);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 404).toString();
        }


        return Responder.makeErrorResponse("Account Deleted Successfully!",200).toString();
    }
}
