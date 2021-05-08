package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.service.authentication.Jwt;
import org.sab.service.validation.HTTPMethod;
import org.sab.validation.Attribute;
import org.sab.validation.DataType;
import org.sab.validation.Schema;

import java.util.HashMap;
import java.util.List;

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
        // retrieving the body objects

        String username = body.getString(USERNAME);
        String password = body.getString(PASSWORD);

        // Authentication
        JSONObject userAuth = authenticateUser(username, password);
        if (userAuth.getInt("statusCode") != 200)
            return userAuth.toString();
        // Add token parameters
        HashMap<String, String> claims = new HashMap<String, String>();
        claims.put("username", USERNAME);
        // Generate Authentication headers
        String token = Jwt.generateToken(claims, 1);

        return makeJwtResponse("Login Successful!", token);
    }
    public static String makeJwtResponse(String msg,String token) {
        JSONObject response = new JSONObject().put("msg", msg).put("token",token);
        response.put("statusCode", 200);
        return response.toString();
    }

}
