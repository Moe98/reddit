package org.sab.models.user;

import org.json.JSONObject;
import org.sab.functions.Utilities;

public class User {

    private String userId;
    private String username;
    private String password;
    private String photoUrl;
    private String birthdate;
    private String email;
    private int numOfFollowers;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNumOfFollowers() {
        return numOfFollowers;
    }

    public void setNumOfFollowers(int numOfFollowers) {
        this.numOfFollowers = numOfFollowers;
    }

    public static String getCollectionName() {
        return "Users";
    }

    public String reformatUserId() {
        return Utilities.formatUUID(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", birthdate='" + birthdate + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public JSONObject toJSON() {
        JSONObject user = new JSONObject();
        user.put("userId", userId);
        user.put("username", username);
        user.put("birthdate", birthdate);
        user.put("email", email);
        user.put("photoUrl", photoUrl);
        user.put("password", password);
        return user;
    }

}
