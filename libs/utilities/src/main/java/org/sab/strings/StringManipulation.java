package org.sab.strings;

public class StringManipulation {
    private StringManipulation() {
    }

    public static String camelToPascalCase(String camelCase) {
        char[] copy = camelCase.toCharArray();
        copy[0] = (copy[0] + "").toUpperCase().charAt(0);
        return new String(copy);
    }

    public static String camelToSnakeCase(String camelcase) {
        StringBuilder snakeCase = new StringBuilder();
        for (char c : camelcase.toCharArray())
            if (isCapital(c))
                snakeCase.append("_").append((c + "").toLowerCase());
            else
                snakeCase.append(c);
        return snakeCase.toString();

    }

    public static boolean isCapital(char c) {
        return c >= 'A' && c <= 'Z';
    }
}
