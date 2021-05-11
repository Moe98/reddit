package org.sab.models.user;

import org.sab.functions.Utilities;

import java.sql.ResultSet;
import java.sql.SQLException;

public enum UserAttributes {

    ACTION_MAKER_ID("userId") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) {
        }
    },
    USER_ID("userId") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) throws SQLException {
            user.setUserId(resultSet.getString(USER_ID.getPostgresDb()));
        }
    },
    IS_DELETED("isDeleted") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) {
        }
    },
    DATE_CREATED("dateCreated") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) {
        }
    },
    NUM_OF_FOLLOWERS("numOfFollowers") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) throws SQLException {
            user.setNumOfFollowers(resultSet.getInt(NUM_OF_FOLLOWERS.getPostgresDb()));
        }
    },
    PASSWORD("password") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) throws SQLException {
            user.setPassword(resultSet.getString(PASSWORD.getPostgresDb()));
        }
    },
    USERNAME("username") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) throws SQLException {
            user.setUsername(resultSet.getString(USERNAME.getPostgresDb()));
        }
    },
    EMAIL("email") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) throws SQLException {
            user.setEmail(resultSet.getString(EMAIL.getPostgresDb()));
        }

    },
    BIRTHDATE("birthdate") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) throws SQLException {
            user.setBirthdate(resultSet.getString(BIRTHDATE.getPostgresDb()));
        }
    },
    PHOTO_URL("photoUrl") {
        @Override
        public void setAttribute(User user, ResultSet resultSet) throws SQLException {
            user.setPhotoUrl(resultSet.getString(PHOTO_URL.getPostgresDb()));
        }
    };


    private final String CAMELCASE;

    UserAttributes(String camelCase) {
        this.CAMELCASE = camelCase;
    }

    public String getHTTP() {
        return CAMELCASE;
    }

    public String getArangoDb() {
        return Utilities.camelToPascalCase(CAMELCASE);
    }

    public String getPostgresDb() {
        return Utilities.camelToSnakeCase(CAMELCASE);
    }

    @Override
    public String toString() {
        return CAMELCASE;
    }

    public abstract void setAttribute(User user, ResultSet resultSet) throws SQLException;
}
