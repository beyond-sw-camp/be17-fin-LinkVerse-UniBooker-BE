package org.example.unibooker;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenTest {
    @Test
    void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("=== HASH START ===");
        System.out.println(encoder.encode("super1234!"));
        System.out.println("=== HASH END ===");
    }
}
