package de.emc.mitglieder;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "test1234";
        String hash = encoder.encode(rawPassword);

        System.out.println(hash);
    }
}