package com.DACN.quanlikhoa.service;

import com.DACN.quanlikhoa.dto.PageResponse;
import com.DACN.quanlikhoa.dto.UserCreateRequest;
import com.DACN.quanlikhoa.dto.UserDTO;
import com.DACN.quanlikhoa.dto.UserUpdateRequest;
import com.DACN.quanlikhoa.entity.Role;
import com.DACN.quanlikhoa.entity.User;
import com.DACN.quanlikhoa.repository.RoleRepository;
import com.DACN.quanlikhoa.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý các chức năng CRUD User cho Admin
 * 
 * File: AdminService.java
 * Location: src/main/java/com/DACN/quanlikhoa/service/AdminService.java
 * 
 * KHÔNG ẢNH HƯỞNG đến Authentication Service
 */
@Service
public class AdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Lấy danh sách users có phân trang, tìm kiếm, lọc, sắp xếp
     * 
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số items mỗi trang
     * @param search Từ khóa tìm kiếm (username, email, fullName)
     * @param roleId Filter theo role
     * @param isActive Filter theo trạng thái active
     * @param sortBy Sắp xếp theo field (createdAt, username, fullName, email)
     * @param sortDirection Hướng sắp xếp (asc, desc)
     * @return PageResponse chứa danh sách UserDTO
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getUsers(
            Integer page,
            Integer size,
            String search,
            Integer roleId,
            Boolean isActive,
            String sortBy,
            String sortDirection
    ) {
        logger.info("AdminService.getUsers - page={}, size={}, search={}, roleId={}, isActive={}", 
                page, size, search, roleId, isActive);
        
        // Validate và tạo Sort
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        // Validate sortBy field
        String validSortBy = validateSortField(sortBy);
        Sort sort = Sort.by(direction, validSortBy);
        
        // Tạo Pageable
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Query users dựa trên filters
        Page<User> userPage = getUserPage(search, roleId, isActive, pageable);
        
        // Convert sang UserDTO
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // Build PageResponse
        PageResponse<UserDTO> response = PageResponse.<UserDTO>builder()
                .content(userDTOs)
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
        
        logger.info("Trả về {} users, tổng {} records", userDTOs.size(), userPage.getTotalElements());
        return response;
    }
    
    /**
     * Lấy thông tin chi tiết 1 user theo ID
     * 
     * @param userId User ID
     * @return UserDTO
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Integer userId) {
        logger.info("AdminService.getUserById - userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));
        
        return convertToDTO(user);
    }
    
    /**
     * Tạo user mới
     * 
     * @param request UserCreateRequest
     * @return UserDTO của user vừa tạo
     */
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        logger.info("AdminService.createUser - username={}", request.getUsername());
        
        // Validate username unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại: " + request.getUsername());
        }
        
        // Validate email unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại: " + request.getEmail());
        }
        
        // Validate role exists
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại với ID: " + request.getRoleId()));
        
        // Create User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setFullName(request.getFullName());
        user.setRole(role);
        user.setAvatarUrl(request.getAvatarUrl());
        user.setIsActive(true); // Mặc định active
        
        // Save to database
        User savedUser = userRepository.save(user);
        
        logger.info("Tạo user thành công - userId={}, username={}", 
                savedUser.getUserId(), savedUser.getUsername());
        
        return convertToDTO(savedUser);
    }
    
    /**
     * Cập nhật thông tin user
     * 
     * @param userId User ID cần update
     * @param request UserUpdateRequest chứa thông tin mới
     * @return UserDTO sau khi update
     */
    @Transactional
    public UserDTO updateUser(Integer userId, UserUpdateRequest request) {
        logger.info("AdminService.updateUser - userId={}", userId);
        
        // Load user từ database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));
        
        // Update email (nếu có thay đổi)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Validate email unique
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã tồn tại: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        
        // Update phone
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        // Update fullName
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        // Update role
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại với ID: " + request.getRoleId()));
            user.setRole(role);
        }
        
        // Update avatar
        if (request.getAvatarUrl() != null) {
            // Xóa avatar cũ nếu có
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    fileStorageService.deleteFile(user.getAvatarUrl());
                } catch (Exception e) {
                    logger.warn("Không thể xóa avatar cũ: {}", e.getMessage());
                }
            }
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        // Update isActive
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        // Update password (nếu có)
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }
        
        // Save changes
        User updatedUser = userRepository.save(user);
        
        logger.info("Cập nhật user thành công - userId={}", userId);
        return convertToDTO(updatedUser);
    }
    
    /**
     * Xóa user (Soft Delete - set isActive = false)
     * 
     * @param userId User ID cần xóa
     */
    @Transactional
    public void deleteUser(Integer userId) {
        logger.info("AdminService.deleteUser - userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));
        
        // Soft delete - set isActive = false
        user.setIsActive(false);
        userRepository.save(user);
        
        logger.info("Xóa user thành công (soft delete) - userId={}, username={}", 
                userId, user.getUsername());
    }
    
    /**
     * Khôi phục user đã xóa (set isActive = true)
     * 
     * @param userId User ID cần khôi phục
     * @return UserDTO sau khi khôi phục
     */
    @Transactional
    public UserDTO restoreUser(Integer userId) {
        logger.info("AdminService.restoreUser - userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));
        
        user.setIsActive(true);
        User restoredUser = userRepository.save(user);
        
        logger.info("Khôi phục user thành công - userId={}, username={}", 
                userId, user.getUsername());
        
        return convertToDTO(restoredUser);
    }
    
    /**
     * Bật/tắt trạng thái active của user
     * 
     * @param userId User ID
     * @return UserDTO sau khi toggle
     */
    @Transactional
    public UserDTO toggleUserStatus(Integer userId) {
        logger.info("AdminService.toggleUserStatus - userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));
        
        // Toggle status
        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);
        
        logger.info("Toggle status thành công - userId={}, newStatus={}", 
                userId, updatedUser.getIsActive());
        
        return convertToDTO(updatedUser);
    }
    
    /**
     * Lấy danh sách tất cả roles
     * 
     * @return List<Role> sắp xếp theo priorityLevel
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        logger.info("AdminService.getAllRoles");
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "priorityLevel"));
    }
    
    /**
     * Lấy thống kê users
     * 
     * @return Map chứa thống kê
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        logger.info("AdminService.getUserStatistics");
        
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countActiveUsers();
        Long inactiveUsers = userRepository.countInactiveUsers();
        
        return UserStatistics.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .build();
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    /**
     * Lấy Page<User> dựa trên filters
     */
    private Page<User> getUserPage(String search, Integer roleId, Boolean isActive, Pageable pageable) {
        // Chuẩn hóa search keyword
        String searchKeyword = (search != null && !search.trim().isEmpty()) 
                ? search.trim() 
                : null;
        
        // Query dựa trên combination của filters
        if (searchKeyword != null) {
            // Có search keyword
            if (roleId != null && isActive != null) {
                return userRepository.findBySearchAndRoleAndStatus(searchKeyword, roleId, isActive, pageable);
            } else if (roleId != null) {
                return userRepository.findBySearchAndRole(searchKeyword, roleId, pageable);
            } else if (isActive != null) {
                return userRepository.findBySearchAndStatus(searchKeyword, isActive, pageable);
            } else {
                return userRepository.findBySearch(searchKeyword, pageable);
            }
        } else {
            // Không có search keyword
            if (roleId != null && isActive != null) {
                return userRepository.findByRoleIdAndIsActive(roleId, isActive, pageable);
            } else if (roleId != null) {
                return userRepository.findByRoleId(roleId, pageable);
            } else if (isActive != null) {
                return userRepository.findByIsActive(isActive, pageable);
            } else {
                return userRepository.findAll(pageable);
            }
        }
    }
    
    /**
     * Validate sortBy field
     */
    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdAt"; // Default
        }
        
        // Whitelist allowed fields
        List<String> allowedFields = List.of(
                "userId", "username", "email", "fullName", 
                "createdAt", "updatedAt", "lastLogin", "isActive"
        );
        
        if (allowedFields.contains(sortBy)) {
            return sortBy;
        }
        
        logger.warn("Invalid sortBy field: {}, using default 'createdAt'", sortBy);
        return "createdAt";
    }
    
    /**
     * Convert User entity sang UserDTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setFullName(user.getFullName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRoleId(user.getRole().getRoleId());
        dto.setRoleName(user.getRole().getRoleName());
        dto.setRoleDescription(user.getRole().getRoleDescription());
        dto.setIsActive(user.getIsActive());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
    
    
    /**
     * Inner class cho User Statistics
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserStatistics {
        private Long totalUsers;
        private Long activeUsers;
        private Long inactiveUsers;
    }
}