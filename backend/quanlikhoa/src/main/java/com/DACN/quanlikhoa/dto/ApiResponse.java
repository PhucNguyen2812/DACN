package com.DACN.quanlikhoa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho API response chung
 * 
 * File: ApiResponse.java
 * Location: src/main/java/com/DACN/quanlikhoa/dto/ApiResponse.java
 * 
 * Cấu trúc response chuẩn cho tất cả API
 * {
 *   "success": true,
 *   "message": "Login successful",
 *   "data": { ... },
 *   "timestamp": "2025-01-15T10:30:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    
    /**
     * Trạng thái thành công/thất bại
     * true = Success, false = Error
     */
    private Boolean success;
    
    /**
     * Thông báo cho user
     */
    private String message;
    
    /**
     * Dữ liệu trả về (generic type)
     * Có thể là LoginResponse, UserDTO, List<UserDTO>, ...
     */
    private T data;
    
    /**
     * Thời gian response
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Helper method: Tạo response thành công
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Helper method: Tạo response lỗi
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}