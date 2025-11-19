package com.DACN.quanlikhoa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO để trả về thông tin User cho client
 * 
 * File: UserDTO.java
 * Location: src/main/java/com/DACN/quanlikhoa/dto/UserDTO.java
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Integer userId;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String avatarUrl;
    
    // Role info
    private Integer roleId;
    private String roleName;
    private String roleDescription;
    
    // Status
    private Boolean isActive;
    private LocalDateTime lastLogin;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}