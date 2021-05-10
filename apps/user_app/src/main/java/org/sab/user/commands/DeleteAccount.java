package org.sab.user.commands;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.functions.CloudUtilities;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
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
import java.util.Map;

public class DeleteAccount extends UserCommand {


    protected Schema getSchema() {
        Attribute password = new Attribute(PASSWORD, DataType.PASSWORD, true);
        return new Schema(List.of(password));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.DELETE;
    }

    @Override
    protected String execute() {
        boolean authenticated = authenticationParams.getBoolean(AUTHENTICATED);
        if (!authenticated)
            return Responder.makeErrorResponse("Unauthorized action! Please Login!", 401);

        // retrieving the body objects
        String username = authenticationParams.getString(USERNAME);
        String password = body.getString(PASSWORD);

        // Authentication
        JSONObject userAuth = authenticateUser(username, password);
        if (userAuth.getInt("statusCode") != 200)
            return userAuth.toString();

        //calling the delete SQL procedure
        try {
            PostgresConnection.call("delete_user", username);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
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
        BaseDocument user = new BaseDocument(new HashMap<>(Map.of("is_deleted", true)));
        user.setKey(username);
        Arango arango = Arango.getInstance();
        arango.connectIfNotConnected();
        arango.updateDocument(UserApp.ARANGO_DB_NAME, User.getCollectionName(), user, username);
    }
}
