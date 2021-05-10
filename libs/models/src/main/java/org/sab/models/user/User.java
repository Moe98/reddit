//package org.sab.models.user;
//
//import org.json.JSONObject;
//
//// This class is unused! I will probable delete it later or integrate it with the other model (the one created for postgres)
//
//public class User {
//    String id;
//    boolean isDeleted;
//    String dateCreated;
//
//    public User(String id, boolean isDeleted) {
//        this.id = id;
//        this.isDeleted = isDeleted;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public boolean isDeleted() {
//        return isDeleted;
//    }
//
//    public void setDeleted(boolean deleted) {
//        isDeleted = deleted;
//    }
//
//    @Override
//    public String toString() {
//        return "User{" +
//                "id='" + id + '\'' +
//                ", isDeleted=" + isDeleted +
//                '}';
//    }
//
//    public JSONObject toJSON() {
//        JSONObject user = new JSONObject();
//        user.put(UserAttributes.USER_ID.getHTTP(), id);
//        user.put(UserAttributes.IS_DELETED.getHTTP(), isDeleted);
//        user.put(UserAttributes.DATE_CREATED.getHTTP(), dateCreated);
//        return user;
//    }
//}
