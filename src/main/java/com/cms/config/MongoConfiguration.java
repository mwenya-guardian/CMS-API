package com.cms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class MongoConfiguration {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(
                // fetch username from SecurityContextHolder
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }
}

