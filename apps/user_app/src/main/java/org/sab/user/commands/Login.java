package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.auth.Jwt;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.List;
import java.util.Map;

public class Login extends UserCommand {


    protected Schema getSchema() {
        Attribute username = new Attribute(USERNAME, DataType.USERNAME, true);
        Attribute password = new Attribute(PASSWORD, DataType.PASSWORD, true);
        return new Schema(List.of(username, password));
    }

    @Override
    protected HTTPMethod getMethodType() {
        return HTTPMethod.POST;
    }

    @Override
    protected String execute() {
        String username = body.getString(USERNAME);
        String password = body.getString(PASSWORD);

        JSONObject userAuth = authenticateUser(username, password);
        if (userAuth.getInt("statusCode") != 200)
            return userAuth.toString();

        Map<String, String> claims = Map.of(USERNAME, username);
        String token = Jwt.generateToken(claims, 60);

        return makeJwtResponse(token);
    }

    private static String makeJwtResponse(String token) {
        JSONObject response = new JSONObject().put("msg", "Login Successful!").put("token", token);
        response.put("statusCode", 200);
        return response.toString();
    }

}
