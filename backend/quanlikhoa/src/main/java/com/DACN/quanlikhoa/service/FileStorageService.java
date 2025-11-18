package com.DACN.quanlikhoa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service xử lý upload file avatar
 * 
 * File: FileStorageService.java
 * Location: src/main/java/com/DACN/quanlikhoa/service/FileStorageService.java
 */
@Service
public class FileStorageService {
    
    private final Path fileStorageLocation;
    
    public FileStorageService(@Value("${file.upload-dir:uploads/avatars}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục upload: " + ex.getMessage());
        }
    }
    
    /**
     * Lưu file avatar và trả về URL
     * 
     * @param file MultipartFile từ request
     * @return URL của file đã upload
     */
    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);
        
        // Tạo tên file unique
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        try {
            // Copy file vào thư mục storage
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Trả về URL (relative path)
            return "/uploads/avatars/" + newFilename;
            
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file: " + ex.getMessage());
        }
    }
    
    /**
     * Xóa file avatar cũ
     * 
     * @param fileUrl URL của file cần xóa
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            // Lấy filename từ URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = this.fileStorageLocation.resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log error nhưng không throw exception
            System.err.println("Không thể xóa file: " + ex.getMessage());
        }
    }
    
    /**
     * Validate file upload
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        
        // Check file size (max 2MB)
        long maxSize = 2 * 1024 * 1024; // 2MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File không được vượt quá 2MB");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (jpg, png, gif)");
        }
        
        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }
    }
    
    /**
     * Lấy extension của file
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf(".");
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }
}