package security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * See https://crackstation.net/hashing-security.htm and https://www.baeldung.com/java-password-hashing.
 */

public class HashedPassword {
    private byte[] salt;
    private byte[] hash;

    /**
     * Constant-time byte array comparison.
     * Source: https://crackstation.net/hashing-security.htm
     * @param a A byte array to compare with b.
     * @param b A byte array to compare with a.
     * @return Returns true if they are equal, false if they are not.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    private void hashPassword(byte[] salt, String password) {
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, SecurityParameters.PASSWORD_HASH_ITERATION_COUNT, SecurityParameters.PASSWORD_HASH_LENGTH_BYTES * 8);
        SecretKeyFactory secretKeyFactory;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            hash = secretKeyFactory.generateSecret(keySpec).getEncoded();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("\033[1;31mFATAL ERROR: Cannot find password hashing algorithm 'PBKDF2WithHmacSHA512'.\033[0m");
            System.exit(-1);
        }
        catch (InvalidKeySpecException e) {
            e.printStackTrace();
            System.out.println("\033[1;31mFATAL ERROR: Invalid key spec exception.\033[0m");
            System.exit(-1);
        }
    }

    public HashedPassword(byte[] salt, byte[] hash) {
        this.salt = salt;
        this.hash = hash;
    }

    public HashedPassword(String password) {
        SecureRandom rng = new SecureRandom();
        salt = new byte[SecurityParameters.PASSWORD_SALT_LENGTH_BYTES];
        rng.nextBytes(salt);
        hashPassword(salt, password);
    }

    public HashedPassword(byte[] salt, String password) {
        hashPassword(salt, password);
    }

    public boolean validate(String password) {
        HashedPassword possiblePassword = new HashedPassword(salt, password);
        byte[] possibleHash = possiblePassword.getHash();
        return slowEquals(possibleHash, hash);
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }
}
