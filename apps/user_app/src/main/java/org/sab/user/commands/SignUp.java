package org.sab.user.commands;

import com.arangodb.ArangoDBException;
import org.sab.arango.Arango;
import org.sab.auth.Auth;
import org.sab.models.CollectionNames;
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

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class SignUp extends UserCommand {

    @Override
    protected Schema getSchema() {
        Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        Attribute email = new Attribute(EMAIL, DataType.EMAIL, true);
        Attribute password = new Attribute(PASSWORD, DataType.PASSWORD, true);
        Attribute birthdate = new Attribute(BIRTHDATE, DataType.SQL_DATE, true);

        return new Schema(List.of(username, email, password, birthdate));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    protected String execute() {
        String username = body.getString(USERNAME);
        String userId = UUID.randomUUID().toString();
        String hashedPassword = Auth.hash(body.getString(PASSWORD));
        String email = body.getString(EMAIL);
        Date birthdate = Date.valueOf(body.getString(BIRTHDATE));

        try {
            if (isUsernameDeleted(username))
                return Responder.makeMsgResponse("This username is already in use, please try another one.");
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

        try {
            PostgresConnection.call("create_user", userId, username, email, hashedPassword, birthdate);
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

        try {
            InsertUserInArango(username);
        } catch (ArangoDBException e) {
            return Responder.makeErrorResponse("ArangoDB Error: " + e.getMessage(), 500);
        }

        try {
            User user = getUser(username, UserAttributes.USER_ID, UserAttributes.USERNAME, UserAttributes.EMAIL, UserAttributes.BIRTHDATE);
            return Responder.makeDataResponse(user.toJSON());
        } catch (EnvironmentVariableNotLoaded | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

    }

    private void InsertUserInArango(String username) {
        Map<String, Object> properties = Map.of(UserAttributes.IS_DELETED.getArangoDb(), false, UserAttributes.NUM_OF_FOLLOWERS.getArangoDb(), 0);

        Arango.createDocument(UserApp.ARANGO_DB_NAME, CollectionNames.USER.get(), properties, username);
    }

    private boolean isUsernameDeleted(String username) throws SQLException, EnvironmentVariableNotLoaded {
        ResultSet resultSet = PostgresConnection.call("is_username_deleted", username);
        resultSet.next();
        return resultSet.getBoolean(1);
    }


}
