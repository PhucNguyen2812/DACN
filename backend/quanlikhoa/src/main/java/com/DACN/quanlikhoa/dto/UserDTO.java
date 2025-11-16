package com.DACN.quanlikhoa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin user (không bao gồm password)
 * 
 * File: UserDTO.java
 * Location: src/main/java/com/DACN/quanlikhoa/dto/UserDTO.java
 * 
 * Dùng để trả về thông tin user cho client
 * ⚠️ KHÔNG BAO GIỜ trả về passwordHash trong response
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
    private String roleName;
    private String roleDescription;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    
    // ⚠️ KHÔNG có field passwordHash - bảo mật!
}