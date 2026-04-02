package com.sobready.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves static files (uploaded images) via URL.
 *
 * NestJS equivalent:
 *   app.useStaticAssets(join(__dirname, '..', 'uploads'), { prefix: '/uploads' });
 *
 * or Express:
 *   app.use('/uploads', express.static('uploads'));
 *
 * This maps:
 *   URL:  http://localhost:8080/uploads/abc123.jpg
 *   File: src/main/resources/static/uploads/abc123.jpg
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:src/main/resources/static/uploads/");
    }
}
