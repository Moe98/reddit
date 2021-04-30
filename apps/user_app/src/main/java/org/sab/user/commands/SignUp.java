package org.sab.user.commands;

import org.json.JSONObject;
import org.sab.models.User;
import org.sab.postgres.PostgresConnection;
import org.sab.postgres.exceptions.PropertiesNotLoadedException;
import org.sab.service.Command;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

public class SignUp extends Command {

    public JSONObject error(String msg,int statusCode){
        JSONObject error=new JSONObject().put("msg",msg).put("statusCode",statusCode);
        return error;
    }
    //TODO, introduce constraints on the inputs
    public String verifyBody(JSONObject body){
        Set<String> keySet=body.keySet();
        String[]params={"username","email","password","birthdate"};
        String missing="";
        for(String param:params)
            if(!keySet.contains(param)){
                if(!missing.equals(""))
                    missing+=", ";
                missing+=param;
            }
        return missing.equals("")?null:String.format("You must insert %s in the request body",missing);

    }
    @Override
    public String execute(JSONObject request) {

        JSONObject body = request.getJSONObject("body");
        String verifyBody=verifyBody(body);

        if(verifyBody!=null)
            return error(verifyBody,404).toString();
        String userId = UUID.randomUUID().toString();

        String username = body.getString("username");
        String hashedPassword = null;
        try {
            hashedPassword = encrypt(body.getString("password"));
        } catch (NoSuchAlgorithmException |UnsupportedEncodingException e) {
           return error(e.getMessage(),404).toString();
        }
        String email = body.getString("email");
        String photoUrl = body.keySet().contains("photo_url") ? body.getString("photo_url") : null;

        Date birthdate = Date.valueOf(body.getString("birthdate"));
        try {
            PostgresConnection.call("create_user", new Object[]{userId, username, email, hashedPassword, birthdate, photoUrl});
        } catch (PropertiesNotLoadedException|SQLException e) {
            return error(e.getMessage(),404).toString();
        }
        User user = null;
        try {
            ResultSet resultSet = PostgresConnection.call("get_user", new Object[]{username});

            if (resultSet == null || !resultSet.next()) {
                return error("ResultSet is Empty!",404).toString();
            }
            //TODO: use builder pattern
            user = new User();

            user.setUserId(resultSet.getString("user_id"));
            user.setUsername(resultSet.getString("username"));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("password"));
            user.setBirthdate(resultSet.getString("birthdate"));
            user.setPhotoUrl(resultSet.getString("photo_url"));

        } catch (PropertiesNotLoadedException|SQLException e) {
            return error("ResultSet is Empty!",404).toString();
        }
        return String.format("{\"data\":%s,\"statusCode\":200}", user.toJSON());
    }

    public String encrypt(String plainTextPassword) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest salt = MessageDigest.getInstance("SHA-256");
        salt.update(plainTextPassword.toString().getBytes("UTF-8"));
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
