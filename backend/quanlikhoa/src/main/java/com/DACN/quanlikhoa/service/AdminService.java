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
 * Service xử lý chức năng quản lý users cho Admin
 * 
 * File: AdminService.java
 * Location: src/main/java/com/DACN/quanlikhoa/service/AdminService.java
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
     * Lấy danh sách users có phân trang, tìm kiếm, lọc
     * 
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số items mỗi trang
     * @param search Từ khóa tìm kiếm (username, email, fullName)
     * @param roleId Filter theo role
     * @param isActive Filter theo trạng thái
     * @param sortBy Sắp xếp theo field nào (createdAt, username, fullName)
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
        // Tạo sort
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");
        
        // Tạo pageable
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Query users
        Page<User> userPage;
        
        if (search != null && !search.trim().isEmpty()) {
            // Có search keyword
            if (roleId != null && isActive != null) {
                userPage = userRepository.findBySearchAndRoleAndStatus(
                        search.trim(), roleId, isActive, pageable);
            } else if (roleId != null) {
                userPage = userRepository.findBySearchAndRole(search.trim(), roleId, pageable);
            } else if (isActive != null) {
                userPage = userRepository.findBySearchAndStatus(search.trim(), isActive, pageable);
            } else {
                userPage = userRepository.findBySearch(search.trim(), pageable);
            }
        } else {
            // Không có search
            if (roleId != null && isActive != null) {
                userPage = userRepository.findByRoleIdAndIsActive(roleId, isActive, pageable);
            } else if (roleId != null) {
                userPage = userRepository.findByRoleId(roleId, pageable);
            } else if (isActive != null) {
                userPage = userRepository.findByIsActive(isActive, pageable);
            } else {
                userPage = userRepository.findAll(pageable);
            }
        }
        
        // Convert sang UserDTO
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // Build PageResponse
        return PageResponse.<UserDTO>builder()
                .content(userDTOs)
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }
    
    /**
     * Lấy thông tin chi tiết 1 user
     * 
     * @param userId User ID
     * @return UserDTO
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Integer userId) {
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
        logger.info("Tạo user mới: {}", request.getUsername());
        
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
        
        // Create user entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setFullName(request.getFullName());
        user.setRole(role);
        user.setAvatarUrl(request.getAvatarUrl());
        user.setIsActive(true); // Mặc định active
        
        // Save to DB
        User savedUser = userRepository.save(user);
        
        logger.info("Tạo user thành công: ID={}, Username={}", savedUser.getUserId(), savedUser.getUsername());
        return convertToDTO(savedUser);
    }
    
    /**
     * Cập nhật thông tin user
     * 
     * @param userId User ID
     * @param request UserUpdateRequest
     * @return UserDTO sau khi update
     */
    @Transactional
    public UserDTO updateUser(Integer userId, UserUpdateRequest request) {
        logger.info("Cập nhật user ID: {}", userId);
        
        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));
        
        // Validate email unique (nếu thay đổi)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã tồn tại: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        
        // Update các field khác
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại với ID: " + request.getRoleId()));
            user.setRole(role);
        }
        
        if (request.getAvatarUrl() != null) {
            // Xóa avatar cũ nếu có
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                fileStorageService.deleteFile(user.getAvatarUrl());
            }
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        // Save
        User updatedUser = userRepository.save(user);
        
        logger.info("Cập nhật user thành công: ID={}", userId);
        return convertToDTO(updatedUser);
    }
    
    /**
     * Xóa user (soft delete - set isActive = false)
     * 
     * @param userId User ID
     */
    @Transactional
    public void deleteUser(Integer userId) {
        logger.info("Xóa user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));
        
        // Soft delete
        user.setIsActive(false);
        userRepository.save(user);
        
        logger.info("Xóa user thành công: ID={}, Username={}", userId, user.getUsername());
    }
    
    /**
     * Bật/tắt trạng thái active của user
     * 
     * @param userId User ID
     * @return UserDTO sau khi toggle
     */
    @Transactional
    public UserDTO toggleUserStatus(Integer userId) {
        logger.info("Toggle status user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với ID: " + userId));
        
        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);
        
        logger.info("Toggle status thành công: ID={}, isActive={}", userId, updatedUser.getIsActive());
        return convertToDTO(updatedUser);
    }
    
    /**
     * Lấy danh sách tất cả roles
     * 
     * @return List<Role>
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "priorityLevel"));
    }
    
    /**
     * Convert User entity sang UserDTO
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