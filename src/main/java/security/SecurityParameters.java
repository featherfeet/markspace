package security;

/**
 * Sets the parameters of the password hashing/salting used for security.
 * Do not change any of these values, except to increase them over time as computer processing power makes hashes easier to break.
 * Note that changing any of these values invalidates all existing passwords/salts in the database.
 * @see security.SecurityParameters
 */

/**
 * Sets the number of hash iterations and hash/salt sizes of the password hashing/salting algorithm.
 * Do not change any of these values, except to increase them over time as computer processing power makes hashes easier to break.
 * Note that changing any of these values invalidates all existing passwords/salts in the database.
 */
class SecurityParameters {
    /**
     * Specifies the number of iterations that the password hashing algorithm should run for. May need to be increased as computers get faster at hash-cracking.
     */
    public static final int PASSWORD_HASH_ITERATION_COUNT = 50000;

    /**
     * Specifies the length in bytes of the password hashes that will be generated to securely store users' passwords.
     */
    public static final int PASSWORD_HASH_LENGTH_BYTES = 64; // 512 bits

    /**
     * Specifies the length in bytes of the password salts that will be generated to securely store users' passwords.
     */
    public static final int PASSWORD_SALT_LENGTH_BYTES = 64; // 512 bits
};