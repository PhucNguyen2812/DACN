package com.DACN.quanlikhoa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Config để serve static files (avatars)
 * 
 * File: FileStorageConfig.java
 * Location: src/main/java/com/DACN/quanlikhoa/config/FileStorageConfig.java
 * 
 * Cấu hình này cho phép truy cập files upload qua URL:
 * http://localhost:8080/api/uploads/avatars/abc-123.jpg
 */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convert relative path to absolute path
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadLocation = "file:" + uploadPath.toString() + "/";
        
        // Map URL pattern to file location
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations(uploadLocation);
    }
}