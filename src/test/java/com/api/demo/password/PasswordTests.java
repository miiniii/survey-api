package com.api.demo.password;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTests {

    @Test
    public void test() throws Exception {

        System.out.println(new BCryptPasswordEncoder().encode("1234"));
        System.out.println(new BCryptPasswordEncoder().encode("1234"));
        System.out.println(new BCryptPasswordEncoder().encode("1234"));
        System.out.println(new BCryptPasswordEncoder().encode("1234"));
        System.out.println(new BCryptPasswordEncoder().encode("1234"));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(passwordEncoder.matches("1234", "$2a$10$XCoytAVzL0qzQ6te6T7LbuWIe9tJwKNp9dxa4/zngGCvksmErvRd.")) {
            System.out.println("matched");
        }
    }
}