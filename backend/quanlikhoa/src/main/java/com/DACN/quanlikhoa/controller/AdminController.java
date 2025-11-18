package com.DACN.quanlikhoa.controller;

import com.DACN.quanlikhoa.dto.ApiResponse;
import com.DACN.quanlikhoa.dto.PageResponse;
import com.DACN.quanlikhoa.dto.UserCreateRequest;
import com.DACN.quanlikhoa.dto.UserDTO;
import com.DACN.quanlikhoa.dto.UserUpdateRequest;
import com.DACN.quanlikhoa.entity.Role;
import com.DACN.quanlikhoa.service.AdminService;
import com.DACN.quanlikhoa.service.FileStorageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin Controller - Quản lý Users
 * 
 * File: AdminController.java
 * Location: src/main/java/com/DACN/quanlikhoa/controller/AdminController.java
 * 
 * REST API endpoints cho Admin quản lý users
 * Base URL: /api/admin
 * 
 * Authorization: Chỉ ADMIN và TRUONG_KHOA mới được truy cập
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'TRUONG_KHOA')")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Lấy danh sách users có phân trang, tìm kiếm, lọc, sắp xếp
     * 
     * GET /api/admin/users?page=0&size=20&search=admin&roleId=1&isActive=true&sortBy=createdAt&sortDirection=desc
     * 
     * Query params:
     * - page: Số trang (default: 0)
     * - size: Số items/trang (default: 20)
     * - search: Từ khóa tìm kiếm (username, email, fullName)
     * - roleId: Filter theo role
     * - isActive: Filter theo status (true/false)
     * - sortBy: Sắp xếp theo field (createdAt, username, fullName)
     * - sortDirection: Hướng sắp xếp (asc, desc)
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Lấy danh sách users thành công",
     *   "data": {
     *     "content": [...],
     *     "currentPage": 0,
     *     "totalPages": 5,
     *     "totalElements": 100,
     *     "pageSize": 20,
     *     "hasNext": true,
     *     "hasPrevious": false
     *   }
     * }
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        logger.info("API /api/admin/users - page={}, size={}, search={}, roleId={}, isActive={}", 
                page, size, search, roleId, isActive);
        
        try {
            PageResponse<UserDTO> result = adminService.getUsers(
                    page, size, search, roleId, isActive, sortBy, sortDirection);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy danh sách users thành công", result)
            );
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thông tin chi tiết 1 user
     * 
     * GET /api/admin/users/{id}
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Lấy thông tin user thành công",
     *   "data": {
     *     "userId": 1,
     *     "username": "admin",
     *     ...
     *   }
     * }
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Integer id) {
        logger.info("API /api/admin/users/{} - Lấy chi tiết user", id);
        
        try {
            UserDTO user = adminService.getUserById(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy thông tin user thành công", user)
            );
        } catch (RuntimeException e) {
            logger.error("Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi lấy user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo user mới
     * 
     * POST /api/admin/users
     * 
     * Request Body:
     * {
     *   "username": "user01",
     *   "password": "123456",
     *   "email": "user01@gmail.com",
     *   "phone": "0123456789",
     *   "fullName": "Nguyễn Văn A",
     *   "roleId": 4,
     *   "avatarUrl": "/uploads/avatars/xyz.jpg"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Tạo user thành công",
     *   "data": { ... }
     * }
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        
        logger.info("API /api/admin/users - Tạo user mới: {}", request.getUsername());
        
        try {
            UserDTO createdUser = adminService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo user thành công", createdUser));
        } catch (RuntimeException e) {
            logger.error("Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi tạo user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Cập nhật thông tin user
     * 
     * PUT /api/admin/users/{id}
     * 
     * Request Body:
     * {
     *   "email": "newemail@gmail.com",
     *   "phone": "0987654321",
     *   "fullName": "Nguyễn Văn B",
     *   "roleId": 5,
     *   "isActive": true
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Cập nhật user thành công",
     *   "data": { ... }
     * }
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequest request) {
        
        logger.info("API /api/admin/users/{} - Cập nhật user", id);
        
        try {
            UserDTO updatedUser = adminService.updateUser(id, request);
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật user thành công", updatedUser)
            );
        } catch (RuntimeException e) {
            logger.error("Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa user (soft delete)
     * 
     * DELETE /api/admin/users/{id}
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Xóa user thành công",
     *   "data": null
     * }
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        logger.info("API /api/admin/users/{} - Xóa user", id);
        
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Xóa user thành công", null)
            );
        } catch (RuntimeException e) {
            logger.error("Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi xóa user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Bật/tắt trạng thái active của user
     * 
     * PUT /api/admin/users/{id}/toggle-status
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Cập nhật trạng thái user thành công",
     *   "data": { ... }
     * }
     */
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserDTO>> toggleUserStatus(@PathVariable Integer id) {
        logger.info("API /api/admin/users/{}/toggle-status", id);
        
        try {
            UserDTO updatedUser = adminService.toggleUserStatus(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật trạng thái user thành công", updatedUser)
            );
        } catch (RuntimeException e) {
            logger.error("Lỗi: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi toggle status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Upload avatar cho user
     * 
     * POST /api/admin/users/upload-avatar
     * 
     * Form-data:
     * - file: MultipartFile (image)
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Upload avatar thành công",
     *   "data": {
     *     "avatarUrl": "/uploads/avatars/abc-123.jpg"
     *   }
     * }
     */
    @PostMapping("/users/upload-avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        
        logger.info("API /api/admin/users/upload-avatar - Upload avatar");
        
        try {
            String avatarUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(
                    ApiResponse.success("Upload avatar thành công", avatarUrl)
            );
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi upload avatar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách tất cả roles
     * 
     * GET /api/admin/roles
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Lấy danh sách roles thành công",
     *   "data": [
     *     {
     *       "roleId": 1,
     *       "roleName": "ADMIN",
     *       "roleDescription": "Quản trị viên hệ thống",
     *       "priorityLevel": 1
     *     },
     *     ...
     *   ]
     * }
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        logger.info("API /api/admin/roles - Lấy danh sách roles");
        
        try {
            List<Role> roles = adminService.getAllRoles();
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy danh sách roles thành công", roles)
            );
        } catch (Exception e) {
            logger.error("Lỗi khi lấy roles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
}