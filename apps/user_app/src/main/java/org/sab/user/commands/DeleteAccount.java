package org.sab.user.commands;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.functions.CloudUtilities;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.user.UserApp;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class DeleteAccount extends UserCommand {


    protected Schema getSchema() {

        Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        Attribute password = new Attribute(PASSWORD, DataType.PASSWORD, true);
        return new Schema(List.of(username, password));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.DELETE;
    }

    @Override
    protected String execute() {
        Boolean authenticated = authenticationParams.getBoolean(Authenticated);
        if(!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);

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
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }
        try {
            deleteFromArango(username);
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB Error: " + e.getMessage(), 500);
        }

        // Deleting profile picture from Cloudinary
        try {
            CloudUtilities.destroyImage(username);
        } catch (IOException | EnvironmentVariableNotLoaded e) {
            return Responder.makeErrorResponse(e.getMessage(), 400);
        }


        return Responder.makeMsgResponse("Account Deleted Successfully!");
    }


    private void deleteFromArango(String username) {
        HashMap<String, Object> documentProperties = new HashMap<>();
        documentProperties.put("is_deleted", true);
        BaseDocument user = new BaseDocument(documentProperties);
        user.setKey(username);
        Arango arango = Arango.getInstance();
        ArangoDB arangoDB = arango.connect();
        arango.updateDocument(arangoDB, UserApp.ARANGO_DB_NAME, "Users", user, username);
    }
}
