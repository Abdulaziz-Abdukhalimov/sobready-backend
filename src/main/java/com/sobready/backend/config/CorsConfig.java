package com.sobready.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS = Cross-Origin Resource Sharing
 *
 * Your React app runs on localhost:3000 (Vite)
 * Your Java backend runs on localhost:8080
 * Browsers block requests between different origins by default.
 * This config tells the browser: "it's OK, allow requests from React"
 *
 * In NestJS you'd do: app.enableCors({ origin: "http://localhost:3000", credentials: true })
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Which frontend URLs can access this API
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"  // Vite's default port
        ));

        // Which HTTP methods are allowed
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Which headers the frontend can send
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies/credentials to be sent (withCredentials: true in Axios)
        config.setAllowCredentials(true);

        // Apply this config to all routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
