package org.sab.functions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtilities {
    public enum Type {
        String, Int, SQLDate, Email;
    }

    final static String ERROR = "";
    final static String OK = null;

    static String isString(Object object) {
        return object instanceof String ? OK : ERROR;
    }

    static String isInt(Object object) {
        return object instanceof Integer ? OK : ERROR;
    }

    static String isSQLDate(Object object) {
        String errorMsg = "SQLDates must be formatted as Strings of the form (yyyy-[m]m-[d]d)";
        if (isString(object) != null)
            return errorMsg;
        try {
            java.sql.Date.valueOf((String) object);
            return OK;
        } catch (IllegalArgumentException e) {
            return errorMsg;
        }

    }

    private static String isEmail(Object object) {
        String errorMsg = "Emails must be formatted as Strings of the form ([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        if (isString(object) != null)
            return errorMsg;
        String email = (String) object;
        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");

        return pattern.matcher(email).matches() ? OK : errorMsg;

    }


    public static String isType(Object object, Type type) {
        switch (type) {
            case Int:
                return isInt(object);
            case SQLDate:
                return isSQLDate(object);
            case String:
                return isString(object);
            case Email:
                return isEmail(object);
        }
        return ERROR;
    }
}


