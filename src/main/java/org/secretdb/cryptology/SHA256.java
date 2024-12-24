package org.secretdb.cryptology;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256{
    private static final Logger logger = LogManager.getLogger(SHA256.class);
    private static final String ALGORITHM = "SHA-256";

    public static String hash(final String input) {
        try {
            final byte[] hashBytes = MessageDigest.getInstance(ALGORITHM).digest(input.getBytes());
            final StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b)); // byte is [0,255] or [-128,127], using %02x hex ("ff" is equivalent 255)
            }
            return hex.toString();
        } catch (final NoSuchAlgorithmException e) {
            logger.error("No such algorithm {}", ALGORITHM);
            throw new RuntimeException("No such algorithm: " + ALGORITHM);
        }
    }
}
