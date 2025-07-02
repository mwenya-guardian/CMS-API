package com.cms.config;

import com.cms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private AuthService authService;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user
        authService.createDefaultAdmin();
        System.out.println("Default admin user created: admin@cms.com / admin123");
    }
}