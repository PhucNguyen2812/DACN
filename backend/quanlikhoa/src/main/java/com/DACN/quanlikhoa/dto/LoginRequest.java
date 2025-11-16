package com.DACN.quanlikhoa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request đăng nhập
 * 
 * File: LoginRequest.java
 * Location: src/main/java/com/DACN/quanlikhoa/dto/LoginRequest.java
 * 
 * Dùng để nhận dữ liệu từ client khi login
 * Request body dạng JSON:
 * {
 *   "username": "admin",
 *   "password": "password123"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * Tên đăng nhập
     * @NotBlank: Không được null, empty hoặc chỉ có khoảng trắng
     */
    @NotBlank(message = "Username không được để trống")
    private String username;
    
    /**
     * Mật khẩu
     * @NotBlank: Không được null, empty hoặc chỉ có khoảng trắng
     */
    @NotBlank(message = "Password không được để trống")
    private String password;
}