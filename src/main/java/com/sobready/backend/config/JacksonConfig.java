package com.sobready.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson = the JSON serializer in Spring Boot.
 * Like how Express uses JSON.stringify() — Jackson converts Java objects to JSON.
 *
 * This config customizes how Java objects become JSON:
 * - Dates are serialized as ISO strings (not timestamps)
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Serialize LocalDateTime as "2024-01-15T10:30:00" instead of a number
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
