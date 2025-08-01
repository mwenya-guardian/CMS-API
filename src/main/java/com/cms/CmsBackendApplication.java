package com.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CmsBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CmsBackendApplication.class, args);
    }
}