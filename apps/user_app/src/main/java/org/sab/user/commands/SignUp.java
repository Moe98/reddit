package org.sab.user.commands;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import org.sab.arango.Arango;
import org.sab.functions.Auth;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Responder;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
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

        // retrieving the body objects
        String username = body.getString(USERNAME);
        String userId = UUID.randomUUID().toString();
        String hashedPassword = Auth.hash(body.getString(PASSWORD));
        String email = body.getString(EMAIL);
        Date birthdate = Date.valueOf(body.getString(BIRTHDATE));

        // Calling the create_user SQL procedure
        try {
            PostgresConnection.call("create_user", userId, username, email, hashedPassword, birthdate);
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }

        try {
            InsertUserInArango(username);
        } catch (Exception e) {
            return Responder.makeErrorResponse(e.getMessage(), 500);
        }


        // getting the user
        try {
            User user = getUser(username, USER_ID, USERNAME, EMAIL, BIRTHDATE);
            return Responder.makeDataResponse(user.toJSON());
        } catch (PropertiesNotLoadedException | SQLException e) {
            return Responder.makeErrorResponse(e.getMessage(), 502);
        }


    }

    private void InsertUserInArango(String username) {
        HashMap<String, Object> documentProperties = new HashMap<>();
        documentProperties.put("is_deleted", false);
        BaseDocument user = new BaseDocument(documentProperties);
        user.setKey(username);
        Arango arango = Arango.getInstance();
        ArangoDB arangoDB = arango.connect();
        arango.createDocument(arangoDB, ARANGO_DB_NAME, "Users", user);

    }


}
