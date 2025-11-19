package com.DACN.quanlikhoa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * File Storage Configuration
 * 
 * File: FileStorageConfig.java
 * Location: src/main/java/com/DACN/quanlikhoa/config/FileStorageConfig.java
 * 
 * Mô tả: Cấu hình cho việc lưu trữ file (avatar, documents, etc.)
 * 
 * Lấy giá trị từ application.properties:
 * file.upload-dir=uploads/avatars
 */
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {
    
    /**
     * Đường dẫn thư mục lưu file
     * Default: uploads/avatars
     * 
     * Có thể là:
     * - Relative path: uploads/avatars (lưu trong project folder)
     * - Absolute path: /var/uploads/avatars (lưu trong hệ thống)
     */
    private String uploadDir = "uploads/avatars";
    
    // Getters and Setters
    public String getUploadDir() {
        return uploadDir;
    }
    
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}