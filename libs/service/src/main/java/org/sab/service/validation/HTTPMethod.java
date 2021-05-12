package org.sab.service.validation;

public enum HTTPMethod {
    GET, PUT, POST, DELETE;

    public boolean equals(String methodType) {
        return toString().equals(methodType);
    }
}
