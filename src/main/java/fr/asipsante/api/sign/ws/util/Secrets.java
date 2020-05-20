/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.ws.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * The Class Secrets.
 */
public class Secrets {

    /** The Constant LOG_ROUNDS. */
    private static final int LOG_ROUNDS = 12;

    /**
     * Instantiates a new secrets.
     */
    private Secrets() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns a hashed password using the provided hash.<br>
     *
     * @param secret the password to be hashed
     * @return the hashed password
     */
    public static String hash(String secret) {
        return BCrypt.hashpw(secret, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Returns true if the given password and salt match the hashed value, false
     * otherwise.<br>
     * Note - side effect: the password is destroyed (the char[] is filled with
     * zeros)
     *
     * @param secret the secret to check
     * @param hash   the expected hashed value of the password
     *
     * @return true if the given password match the hashed value, false otherwise
     */
    public static boolean match(String secret, String hash) {
        return BCrypt.checkpw(secret, hash);
    }

}