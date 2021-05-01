package org.sab.functions;

public class TypeUtilities {
    public enum Type{
        String,Int,SQLDate;
    }
    static boolean isString(Object object){
        return object instanceof String;
    }
    static boolean isInt(Object object){
        return object instanceof Integer;
    }
    static boolean isSQLDate(Object object){
        if(!isString(object))
            return false;
        try{
            java.sql.Date.valueOf((String)object);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }

    }

    public static boolean isType(Object object, Type type){
        switch (type){
            case Int : return isInt(object);
            case SQLDate: return isSQLDate(object);
            case String: return isString(object);
        }
        return false;
    }
}
