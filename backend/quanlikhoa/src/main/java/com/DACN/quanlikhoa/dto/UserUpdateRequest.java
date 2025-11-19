package com.DACN.quanlikhoa.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request cập nhật thông tin user
 * 
 * File: UserUpdateRequest.java
 * Location: src/main/java/com/DACN/quanlikhoa/dto/UserUpdateRequest.java
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;
    
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Số điện thoại không hợp lệ")
    private String phone;
    
    @Size(max = 200, message = "Họ tên tối đa 200 ký tự")
    private String fullName;
    
    private Integer roleId;
    
    @Size(max = 500, message = "Avatar URL tối đa 500 ký tự")
    private String avatarUrl;
    
    private Boolean isActive;
    
    // Password update (optional - nếu muốn đổi password)
    @Size(min = 6, max = 100, message = "Password phải từ 6-100 ký tự")
    private String newPassword;
}