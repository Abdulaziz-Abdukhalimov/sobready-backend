package com.sobready.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is the ENTRY POINT of your app — like main.tsx in React.
 *
 * @SpringBootApplication does 3 things:
 * 1. Marks this as a Spring Boot app
 * 2. Enables auto-configuration (Spring sets up everything automatically)
 * 3. Scans this package for components (@Controller, @Service, @Repository, etc.)
 */
@SpringBootApplication
public class SobreadyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SobreadyApplication.class, args);
        System.out.println("✅ Sobready Backend is running on http://localhost:8080");
    }
}
