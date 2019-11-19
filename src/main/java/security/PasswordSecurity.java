package security;

/**
 * Class for securely verifying and hashing passwords.
 * @see security.PasswordSecurity
 */

/**
 * Class for securely verifying and hashing passwords.
 */
public class PasswordSecurity {
    /**
     * Hash a plaintext password, creating a HashedPassword object.
     * @see security.HashedPassword
     * @param password The plaintext password to hash.
     * @return A security.HashedPassword object created by hashing the password.
     */
    public static HashedPassword hashPassword(String password) {
        return new HashedPassword(password);
    }

    /**
     * Validate a plaintext password against a HashedPassword object. This is designed to be used to compare a user-inputted password (like the one from the login page) with a hash from the database.
     * @param password The plaintext password to validate.
     * @param hashedPassword The hashed password to compare the plaintext password with.
     * @return true if the password was valid. false if the password was invalid.
     */
    public static boolean validatePassword(String password, HashedPassword hashedPassword) {
        return hashedPassword.validate(password);
    }
}
