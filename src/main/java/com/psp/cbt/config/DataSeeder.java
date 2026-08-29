package com.psp.cbt.config;

import com.psp.cbt.entity.Admin;
import com.psp.cbt.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// CommandLineRunner.run() executes once, automatically, every time
// the application starts. We use it to make sure there is always
// at least one admin account to log in with.
@Component
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
            System.out.println("======================================");
            System.out.println("Default admin created:");
            System.out.println("  username: admin");
            System.out.println("  password: admin123");
            System.out.println("Change this after first login.");
            System.out.println("======================================");
        }
    }
}
