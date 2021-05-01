package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EditProfile extends Command {



    @Override
    public String execute(JSONObject request)  {
        System.out.println(request);
        JSONObject body = request.getJSONObject("body");

        String oldPassword = body.getString("oldPassword");
        String newPassword = body.getString("newPassword");
        try {
            PostgresConnection.call("update_user_password", "zoz", newPassword);
        } catch (PropertiesNotLoadedException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
//        PostgresConnection postgresConnection = PostgresConnection.getInstance();
//        ResultSet resultSet = postgresConnection.call(procedureInitializer("update_user_password", 2), postgresConnection.connect(), null, );
        return "{\"msg\":\"You are now an editor!\"}";

    }
//    public String updatePassword(String oldPassword,String newPassword) throws PropertiesNotLoadedException {
//
//        Connection connection =postgresConnection.connect();
//        connection.prepareCall()
//        return null;
//    }

}
