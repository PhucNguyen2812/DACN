package com.DACN.quanlikhoa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS Configuration
 * 
 * File: CorsConfig.java
 * Location: src/main/java/com/DACN/quanlikhoa/config/CorsConfig.java
 * 
 * Mô tả: Cấu hình CORS (Cross-Origin Resource Sharing)
 * Cho phép frontend từ các domain khác gọi API
 */
@Configuration
public class CorsConfig {
    
    /**
     * CORS Configuration Bean
     * 
     * Cho phép:
     * - Origin: http://localhost:3000 (React dev server)
     * - Origin: http://localhost:5173 (Vite dev server)
     * - Origin: http://localhost:8443 (https)
     * - Methods: GET, POST, PUT, DELETE, OPTIONS
     * - Headers: Content-Type, Authorization, etc.
     * - Credentials: true (cho phép gửi cookies)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins
        configuration.addAllowedOrigin("http://localhost:3000");     // React dev server
        configuration.addAllowedOrigin("http://localhost:5173");     // Vite dev server
        configuration.addAllowedOrigin("http://localhost:8443");     // HTTPS
        configuration.addAllowedOrigin("http://127.0.0.1:3000");
        configuration.addAllowedOrigin("http://127.0.0.1:5173");
        // Configuration.addAllowedOriginPattern("https?://.*");     // Cho phép tất cả origins (NOT RECOMMENDED)
        
        // Allowed methods
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("PATCH");
        
        // Allowed headers
        configuration.addAllowedHeader("Content-Type");
        configuration.addAllowedHeader("Authorization");
        configuration.addAllowedHeader("X-Requested-With");
        configuration.addAllowedHeader("Accept");
        configuration.addAllowedHeader("Origin");
        
        // Exposed headers (headers mà frontend có thể truy cập)
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Content-Type");
        
        // Allow credentials (cookies, authentication headers)
        configuration.setAllowCredentials(true);
        
        // Max age (cache time in seconds) - 1 giờ
        configuration.setMaxAge(3600L);
        
        // Register configuration
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}