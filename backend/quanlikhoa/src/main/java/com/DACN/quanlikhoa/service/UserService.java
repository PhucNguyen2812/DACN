package com.DACN.quanlikhoa.service;

import com.DACN.quanlikhoa.dto.UserDTO;
import com.DACN.quanlikhoa.entity.User;
import com.DACN.quanlikhoa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service
 * 
 * File: UserService.java
 * Location: src/main/java/com/DACN/quanlikhoa/service/UserService.java
 * 
 * Service xử lý các operations liên quan đến User
 * (Sẽ được mở rộng ở Giai đoạn 2 - User Management)
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy thông tin user hiện tại
     * 
     * @param username Username
     * @return UserDTO (không có password)
     */
    @Transactional(readOnly = true)
    public UserDTO getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + username));
        
        return convertToDTO(user);
    }
    
    /**
     * Convert User entity sang UserDTO
     * 
     * ⚠️ KHÔNG trả về passwordHash - bảo mật!
     * 
     * @param user User entity
     * @return UserDTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roleName(user.getRole().getRoleName())
                .roleDescription(user.getRole().getRoleDescription())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
    

}