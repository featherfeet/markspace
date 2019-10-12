package security;

public class SecurityParameters {
    public static final int PASSWORD_HASH_ITERATION_COUNT = 50000;
    public static final int PASSWORD_HASH_LENGTH_BYTES = 64; // 512 bits
    public static final int PASSWORD_SALT_LENGTH_BYTES = 64; // 512 bits
};