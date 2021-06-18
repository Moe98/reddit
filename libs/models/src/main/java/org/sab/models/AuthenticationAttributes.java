package org.sab.models;

public enum AuthenticationAttributes {
    AUTHENTICATION_PARAMS("authenticationParams"),
    IS_AUTHENTICATED("isAuthenticated");
;
    private final String value;

    AuthenticationAttributes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
