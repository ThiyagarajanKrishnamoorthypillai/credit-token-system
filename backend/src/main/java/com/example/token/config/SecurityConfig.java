// src/main/java/com/example/token/config/SecurityConfig.java
package com.example.token.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())                    // Disable CSRF for API
      .cors(cors -> {})                                // Enable CORS
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/**").permitAll()        // Allow public access to /api/*
        .anyRequest().authenticated()                  // Secure other endpoints
      )
      .formLogin(form -> form.disable())               // Disable Spring's login form
      .httpBasic(httpBasic -> httpBasic.disable());    // Disable basic auth (optional)

    return http.build();
  }
  
}
