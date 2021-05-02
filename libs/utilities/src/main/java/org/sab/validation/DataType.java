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
    };

    private String additionalErrorMessage;
    public abstract boolean isOfValidType(Object object);

    private DataType(String additionalErrorMessage) {
        this.additionalErrorMessage = additionalErrorMessage;
    }

    public String getAdditionalErrorMessage() {
        return additionalErrorMessage;
    }
}