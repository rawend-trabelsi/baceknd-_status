package com.rawend.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(scanBasePackages = "com.rawend.demo")

public class ErApplication implements CommandLineRunner {

   
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(ErApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        // Check if an admin account already exists
      /* User adminAccount = userRepository.findByRole(Role.ADMIN);
        if (adminAccount == null) {
            // Create a new admin account
            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setUsername("admin");
            admin.setPassword("admin123"); // Temporary password before confirmation
            admin.setConfirmPassword("admin123"); // Confirm password
            admin.setRole(Role.ADMIN);
            admin.setPhone("12345678");

            // Validate that password and confirmPassword match
            if (!admin.getPassword().equals(admin.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match");
            }

            // Encode the password
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            admin.setConfirmPassword(passwordEncoder.encode(admin.getConfirmPassword()));

            // Save the admin account to the database
            userRepository.save(admin);

            System.out.println("Admin account created successfully.");
        } else {
            System.out.println("Admin account already exists.");
        }*/
    }

}
