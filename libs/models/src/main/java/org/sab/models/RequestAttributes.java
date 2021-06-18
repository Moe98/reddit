package org.sab.models;

public enum RequestAttributes {
    BODY("body"),
    METHOD_TYPE("methodType"),
    AUTHENTICATION_PARAMS("authenticationParams"),
    FUNCTION_NAME("functionName"),
    URI_PARAMS("uriParams");

    private final String value;

    RequestAttributes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
