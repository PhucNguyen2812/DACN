package com.DACN.quanlikhoa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response đăng nhập thành công
 * 
 * File: LoginResponse.java
 * Location: src/main/java/com/DACN/quanlikhoa/dto/LoginResponse.java
 * 
 * Trả về cho client sau khi đăng nhập thành công
 * Response body dạng JSON:
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "tokenType": "Bearer",
 *   "userId": 1,
 *   "username": "admin",
 *   "fullName": "Nguyễn Văn Admin",
 *   "email": "admin@hcmunre.edu.vn",
 *   "roleName": "ADMIN",
 *   "roleDescription": "Quản trị viên hệ thống"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Lombok: cho phép build object dễ dàng
public class LoginResponse {
    
    /**
     * JWT Access Token
     * Client sẽ lưu token này và gửi kèm trong mỗi request
     * Header: Authorization: Bearer <accessToken>
     */
    private String accessToken;
    
    /**
     * Loại token (luôn là "Bearer")
     */
    private String tokenType = "Bearer";
    
    /**
     * ID của user
     */
    private Integer userId;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Họ tên đầy đủ
     */
    private String fullName;
    
    /**
     * Email
     */
    private String email;
    
    /**
     * Tên vai trò (ADMIN, TRUONG_KHOA, SINH_VIEN, ...)
     */
    private String roleName;
    
    /**
     * Mô tả vai trò
     */
    private String roleDescription;
}