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
 * Admin Controller - Quản lý Users (CRUD)
 * 
 * File: AdminController.java
 * Location: src/main/java/com/DACN/quanlikhoa/controller/AdminController.java
 * 
 * Base URL: /api/admin
 * Authorization: Chỉ ADMIN và TRUONG_KHOA mới được truy cập
 * 
 * KHÔNG ẢNH HƯỞNG đến Authentication endpoints (/api/auth/*)
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
     * 1. Lấy danh sách users với phân trang, tìm kiếm, lọc, sắp xếp
     * 
     * GET /api/admin/users?page=0&size=20&search=admin&roleId=1&isActive=true&sortBy=createdAt&sortDirection=desc
     * 
     * Query Parameters:
     * - page: Số trang (default: 0)
     * - size: Số items/trang (default: 20, max: 100)
     * - search: Từ khóa tìm kiếm (username, email, fullName)
     * - roleId: Filter theo role ID
     * - isActive: Filter theo status (true/false)
     * - sortBy: Sắp xếp theo field (createdAt, username, fullName, email)
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
        logger.info("GET /api/admin/users - page={}, size={}, search={}, roleId={}, isActive={}, sortBy={}, sortDirection={}", 
                page, size, search, roleId, isActive, sortBy, sortDirection);
        
        try {
            // Validate page và size
            if (page < 0) page = 0;
            if (size < 1) size = 20;
            if (size > 100) size = 100; // Max 100 items per page
            
            PageResponse<UserDTO> result = adminService.getUsers(
                    page, size, search, roleId, isActive, sortBy, sortDirection);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy danh sách users thành công", result)
            );
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 2. Lấy thông tin chi tiết 1 user theo ID
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
     *     "email": "admin@example.com",
     *     ...
     *   }
     * }
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Integer id) {
        logger.info("GET /api/admin/users/{}", id);
        
        try {
            UserDTO user = adminService.getUserById(id);
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy thông tin user thành công", user)
            );
        } catch (RuntimeException e) {
            logger.error("User không tồn tại: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi lấy user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 3. Tạo user mới
     * 
     * POST /api/admin/users
     * Content-Type: application/json
     * 
     * Request Body:
     * {
     *   "username": "user01",
     *   "password": "123456",
     *   "email": "user01@example.com",
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
        
        logger.info("POST /api/admin/users - Tạo user mới: {}", request.getUsername());
        
        try {
            UserDTO createdUser = adminService.createUser(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo user thành công", createdUser));
                    
        } catch (RuntimeException e) {
            logger.error("Lỗi validation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi tạo user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 4. Cập nhật thông tin user
     * 
     * PUT /api/admin/users/{id}
     * Content-Type: application/json
     * 
     * Request Body:
     * {
     *   "email": "newemail@example.com",
     *   "phone": "0987654321",
     *   "fullName": "Nguyễn Văn B",
     *   "roleId": 5,
     *   "isActive": true,
     *   "newPassword": "newpassword123"
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
        
        logger.info("PUT /api/admin/users/{} - Cập nhật user", id);
        
        try {
            UserDTO updatedUser = adminService.updateUser(id, request);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật user thành công", updatedUser)
            );
            
        } catch (RuntimeException e) {
            logger.error("Lỗi validation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 5. Xóa user (Soft Delete - set isActive = false)
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
        logger.info("DELETE /api/admin/users/{} - Xóa user (soft delete)", id);
        
        try {
            adminService.deleteUser(id);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Xóa user thành công", null)
            );
            
        } catch (RuntimeException e) {
            logger.error("User không tồn tại: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi xóa user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 6. Khôi phục user đã xóa
     * 
     * PUT /api/admin/users/{id}/restore
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Khôi phục user thành công",
     *   "data": { ... }
     * }
     */
    @PutMapping("/users/{id}/restore")
    public ResponseEntity<ApiResponse<UserDTO>> restoreUser(@PathVariable Integer id) {
        logger.info("PUT /api/admin/users/{}/restore - Khôi phục user", id);
        
        try {
            UserDTO restoredUser = adminService.restoreUser(id);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Khôi phục user thành công", restoredUser)
            );
            
        } catch (RuntimeException e) {
            logger.error("User không tồn tại: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi khôi phục user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 7. Bật/tắt trạng thái active của user
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
        logger.info("PUT /api/admin/users/{}/toggle-status", id);
        
        try {
            UserDTO updatedUser = adminService.toggleUserStatus(id);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật trạng thái user thành công", updatedUser)
            );
            
        } catch (RuntimeException e) {
            logger.error("User không tồn tại: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi toggle status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 8. Upload avatar cho user
     * 
     * POST /api/admin/users/upload-avatar
     * Content-Type: multipart/form-data
     * 
     * Form-data:
     * - file: MultipartFile (image)
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Upload avatar thành công",
     *   "data": "/uploads/avatars/abc-123.jpg"
     * }
     */
    @PostMapping("/users/upload-avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        
        logger.info("POST /api/admin/users/upload-avatar - Upload avatar");
        
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
            logger.error("Lỗi khi upload avatar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 9. Lấy danh sách tất cả roles
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
        logger.info("GET /api/admin/roles - Lấy danh sách roles");
        
        try {
            List<Role> roles = adminService.getAllRoles();
            
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy danh sách roles thành công", roles)
            );
            
        } catch (Exception e) {
            logger.error("Lỗi khi lấy roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * 10. Lấy thống kê users
     * 
     * GET /api/admin/users/statistics
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Lấy thống kê thành công",
     *   "data": {
     *     "totalUsers": 150,
     *     "activeUsers": 120,
     *     "inactiveUsers": 30
     *   }
     * }
     */
    @GetMapping("/users/statistics")
    public ResponseEntity<ApiResponse<AdminService.UserStatistics>> getUserStatistics() {
        logger.info("GET /api/admin/users/statistics - Lấy thống kê users");
        
        try {
            AdminService.UserStatistics stats = adminService.getUserStatistics();
            
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy thống kê thành công", stats)
            );
            
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thống kê: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }
}