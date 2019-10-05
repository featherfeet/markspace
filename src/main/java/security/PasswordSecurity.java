package security;

import java.security.SecureRandom;

public class PasswordSecurity {

    public PasswordSecurity() {
    }

    public HashedPassword hashPassword(String password) {
        return new HashedPassword(password);
    }

    public boolean validatePassword(String password, HashedPassword hashedPassword) {
        return hashedPassword.validate(password);
    }
}
