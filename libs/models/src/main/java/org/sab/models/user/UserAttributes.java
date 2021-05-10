package org.sab.models.user;

import org.sab.functions.Utilities;

public enum UserAttributes {

    ACTION_MAKER_ID("userId"),
    USER_ID("userId"),
    IS_DELETED("isDeleted"),
    DATE_CREATED("dateCreated"),
    NUM_OF_FOLLOWERS("numOfFollowers");


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
}
