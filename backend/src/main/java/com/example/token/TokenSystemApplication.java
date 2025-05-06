package com.example.token;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration; // 👈 IMPORTANT

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // 👈 EXCLUDE JDBC Auto Config
public class TokenSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(TokenSystemApplication.class, args);
    }
}
