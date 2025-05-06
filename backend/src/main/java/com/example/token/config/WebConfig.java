// src/main/java/com/example/token/config/WebConfig.java
package com.example.token.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull; // Needed for @NonNull annotation

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // Allow React frontend to communicate with the backend
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Allow necessary HTTP methods
                .allowCredentials(true); // Allow cookies and credentials to be sent with requests
    }
}



