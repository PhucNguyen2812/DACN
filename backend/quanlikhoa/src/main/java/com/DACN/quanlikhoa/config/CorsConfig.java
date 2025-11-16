package com.DACN.quanlikhoa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration
 * 
 * File: CorsConfig.java
 * Location: src/main/java/com/DACN/quanlikhoa/config/CorsConfig.java
 * 
 * Cấu hình CORS để Frontend (localhost:3000) có thể gọi Backend (localhost:8080)
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép các origins này
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000"
        ));
        
        // Cho phép tất cả HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Cho phép tất cả headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Expose headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type"
        ));
        
        // Max age của preflight request
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}