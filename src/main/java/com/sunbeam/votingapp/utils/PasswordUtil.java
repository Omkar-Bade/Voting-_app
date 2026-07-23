/*
 * File: PasswordUtil.java
 * Fixed/Why: Implemented secure password hashing using SHA-256 with a random salt.
 * Plaintext password storage is a high security vulnerability. Now passwords will be
 * stored as salt:hash in the database, preventing plaintext leakages.
 */
package com.sunbeam.votingapp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    /**
     * Generates a secure random 16-byte salt, Base64-encoded.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes the plaintext password with the provided salt using SHA-256.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Hashes a plaintext password by generating a new salt and returns the formatted 'salt:hash' string.
     */
    public static String hash(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return salt + ":" + hash;
    }

    /**
     * Verifies if a plaintext password matches the stored 'salt:hash' string.
     */
    public static boolean verify(String password, String storedPassword) {
        if (password == null || storedPassword == null || !storedPassword.contains(":")) {
            return false;
        }
        String[] parts = storedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        String salt = parts[0];
        String expectedHash = parts[1];
        String actualHash = hashPassword(password, salt);
        return expectedHash.equals(actualHash);
    }

    /**
     * Utility main method to generate hashed passwords for existing seed data.
     */
    public static void main(String[] args) {
        String[] originalPasswords = {"ram#123", "shk#123", "mad$234", "anil"};
        System.out.println("Hashed passwords for seed users:");
        for (String pw : originalPasswords) {
            System.out.println(pw + " -> " + hash(pw));
        }
    }
}
