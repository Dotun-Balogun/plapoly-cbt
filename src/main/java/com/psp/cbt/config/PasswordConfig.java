package com.psp.cbt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Makes a BCryptPasswordEncoder available anywhere in the app via
// @Autowired. BCrypt is a one-way hash: we can check a password
// against it, but we can never turn the hash back into the password.
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
