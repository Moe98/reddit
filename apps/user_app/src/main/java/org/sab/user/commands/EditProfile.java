package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Command;

import java.sql.Connection;

public class EditProfile extends Command {

    @Override
    public String execute(JSONObject request) {
        // String oldPassword= request.getString("oldPassword");
        // String newPassword=request.getString("newPassword");

        return "{\"msg\":\"You are now an editor!\"}";

    }
//    public String updatePassword(String oldPassword,String newPassword) throws PropertiesNotLoadedException {
//        PostgresConnection postgresConnection=PostgresConnection.getInstance();
//        Connection connection =postgresConnection.connect();
//        connection.prepareCall()
//        return null;
//    }

}
