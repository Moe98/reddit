package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Command;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.UUID;

public class SignUp extends Command {

    @Override
    public String execute(JSONObject request) {

        JSONObject body = request.getJSONObject("body");
        String userId = UUID.randomUUID().toString();

        String username = body.getString("username");
        String hashedPassword = null;
        try {
            hashedPassword = encrypt(body.getString("password"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String email = body.getString("email");
        String photoUrl=body.keySet().contains("photo_url")?body.getString("photo_url"):null;

        Date birthdate = Date.valueOf(body.getString("birthdate"));
        try {
            PostgresConnection.call("create_user", new Object[]{userId, username, email, hashedPassword,birthdate,photoUrl});
        } catch (PropertiesNotLoadedException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            ResultSet resultSet=PostgresConnection.call("get_user",new Object[]{username});
            resultSet.getString("username");
            System.out.println(resultSet.toString()+"sssssssssssssssssssssssss");
        } catch (PropertiesNotLoadedException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


        return "{\"msg\":\"You just got in!\"}";
    }

    public String encrypt(String plainTextPassword) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest salt = MessageDigest.getInstance("SHA-256");
        salt.update(UUID.randomUUID().toString().getBytes("UTF-8"));
        String digest = bytesToHex(salt.digest());
        return digest;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
