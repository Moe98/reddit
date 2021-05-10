package org.sab.functions;

import org.mindrot.jbcrypt.BCrypt;

public class Auth {
    private Auth() {
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean verifyHash(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }

    }
}
