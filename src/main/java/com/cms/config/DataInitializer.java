package com.cms.config;

import com.cms.dto.response.NewsletterSubscriberResponse;
import com.cms.service.AuthService;
import com.cms.service.GmailSmtpEmailService;
import com.cms.service.NewsletterSubscriberService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private AuthService authService;
    // private NewsletterSubscriberService newsletterSubscriberService;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user
        authService.createDefaultAdmin();
        // NewsletterSubscriberResponse news = newsletterSubscriberService.subscribe("mwenyagenesismg@gmail.com");
        // List<NewsletterSubscriberResponse> list = newsletterSubscriberService.getAll();
        System.out.println("Default admin user created: admin@cms.com / admin123");
        // System.out.println(list.isEmpty());
    }
}