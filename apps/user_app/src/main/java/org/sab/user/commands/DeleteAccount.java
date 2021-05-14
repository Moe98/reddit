package org.sab.user.commands;

import com.arangodb.ArangoDBException;
import org.json.JSONObject;
import org.sab.arango.Arango;
import org.sab.minio.MinIO;
import org.sab.models.user.User;
import org.sab.models.user.UserAttributes;
import org.sab.postgres.PostgresConnection;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.user.UserApp;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;
import org.sab.validation.exceptions.EnvironmentVariableNotLoaded;

import java.sql.SQLException;
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
        boolean authenticated = authenticationParams.getBoolean(IS_AUTHENTICATED);
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

        JSONObject user = userAuth.getJSONObject("data");
        if (user.has(UserAttributes.PHOTO_URL.toString())) {
            try {
                String publicId =  user.getString(UserAttributes.USER_ID.toString()).replaceAll("[-]", "");
                boolean output = MinIO.deleteObject(BUCKETNAME, publicId);
                if (!output)
                    return Responder.makeErrorResponse("Error Occurred While Deleting Your Image!", 404);

            } catch (EnvironmentVariableNotLoaded e) {
                return Responder.makeErrorResponse(e.getMessage(), 400);
            }
        }
        return Responder.makeMsgResponse("Account Deleted Successfully!");
    }


    private void deleteFromArango(String username) {
        try {
            Arango.updateDocument(UserApp.ARANGO_DB_NAME, User.getCollectionName(), Map.of(UserAttributes.IS_DELETED.getArangoDb(), true), username);
        } finally {
            Arango.getInstance().disconnect();
        }
    }
}
