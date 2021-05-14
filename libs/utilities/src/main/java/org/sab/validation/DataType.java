package org.sab.validation;

import java.util.regex.Pattern;

public enum DataType {
    STRING("") {
        @Override
        public boolean isOfValidType(Object object) {
            return object instanceof String;
        }
    }, INT("") {
        @Override
        public boolean isOfValidType(Object object) {
            return object instanceof Integer;
        }
    }, BOOLEAN("") {
        @Override
        public boolean isOfValidType(Object object) {

            if (!STRING.isOfValidType(object)) {
                return false;
            }

            final String booleanString = (String) object;
            Pattern pattern = Pattern.compile("true|false");
            return pattern.matcher(booleanString).matches();

        }
    }, SQL_DATE("SQLDates must be formatted as Strings of the form (yyyy-[m]m-[d]d)") {
        @Override
        public boolean isOfValidType(Object object) {
            if (!STRING.isOfValidType(object)) {
                return false;
            }

            try {
                java.sql.Date.valueOf((String) object);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }, EMAIL("Emails must be formatted as Strings of the form ([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}") {
        @Override
        public boolean isOfValidType(Object object) {
            if (!STRING.isOfValidType(object)) {
                return false;
            }
            
            final String email = (String) object;
            final Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");

            return pattern.matcher(email).matches();
        }
    }, PASSWORD("Passwords must be at least 6 characters long") {
        @Override
        public boolean isOfValidType(Object object) {
            if (!STRING.isOfValidType(object)) {
                return false;
            }
            String password = (String) object;
            return password.length() >= 6;
        }
    }, USERNAME("Usernames must be at least 3 characters long.") {
        @Override
        public boolean isOfValidType(Object object) {
            if (!STRING.isOfValidType(object)) {
                return false;
            }
            String username = (String) object;
            return username.length() >= 3;
        }
    }, UUID("UUIDs must be a valid UUID String") {
        @Override
        public boolean isOfValidType(Object object) {
            try {
                java.util.UUID.fromString((String) object);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }, ARRAY_OF_STRING("") {
        @Override
        public boolean isOfValidType(Object object) {
            return object instanceof String[];
        }
    };

    private String additionalErrorMessage;

    public abstract boolean isOfValidType(Object object);

    DataType(String additionalErrorMessage) {
        this.additionalErrorMessage = additionalErrorMessage;
    }

    public String getAdditionalErrorMessage() {
        return additionalErrorMessage;
    }
}
