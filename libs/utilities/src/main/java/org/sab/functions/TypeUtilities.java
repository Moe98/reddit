package org.sab.functions;

public class TypeUtilities {
    public enum Type {
        String, Int, SQLDate;
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
        String errorMsg = "Dates must be formatted as Strings of the form (yyyy-[m]m-[d]d)";
        if (isString(object) != null)
            return errorMsg;
        try {
            java.sql.Date.valueOf((String) object);
            return OK;
        } catch (IllegalArgumentException e) {
            return errorMsg;
        }

    }

    public static String isType(Object object, Type type) {
        switch (type) {
            case Int:
                return isInt(object);
            case SQLDate:
                return isSQLDate(object);
            case String:
                return isString(object);
        }
        return ERROR;
    }
}
