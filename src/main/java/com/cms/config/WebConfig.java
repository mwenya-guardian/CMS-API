package com.cms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${file.upload.dir.main}")
    private String uploadDir;

    @Value("${file.upload.dir.public}")
    private String uploadPublicSubDir;

    @Value("${server.servlet.context-path:..}")
    private String serverContextPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String relativePath = String.format("%s%s/%s/", "file:", uploadDir, uploadPublicSubDir);
        String uploadPath = uploadDir.replaceAll("[./]", "");
        String resourceUrlOption = String.format("/%s/%s/**",uploadPath, uploadPublicSubDir);
        registry.addResourceHandler(resourceUrlOption)
                .addResourceLocations(relativePath)
                .setCachePeriod(3600) //An hour
                .resourceChain(true);

    }
}