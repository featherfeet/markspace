package security;

public class PasswordSecurity {
    public static HashedPassword hashPassword(String password) {
        return new HashedPassword(password);
    }

    public static boolean validatePassword(String password, HashedPassword hashedPassword) {
        return hashedPassword.validate(password);
    }
}
