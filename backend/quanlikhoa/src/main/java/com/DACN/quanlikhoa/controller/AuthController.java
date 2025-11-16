package com.DACN.quanlikhoa.controller;

import com.DACN.quanlikhoa.dto.ApiResponse;
import com.DACN.quanlikhoa.dto.LoginRequest;
import com.DACN.quanlikhoa.dto.LoginResponse;
import com.DACN.quanlikhoa.dto.UserDTO;
import com.DACN.quanlikhoa.service.AuthService;
import com.DACN.quanlikhoa.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * File: AuthController.java
 * Location: src/main/java/com/DACN/quanlikhoa/controller/AuthController.java
 * 
 * REST API endpoints cho authentication
 * Base URL: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Login endpoint
     * 
     * POST /api/auth/login
     * 
     * Request Body:
     * {
     *   "username": "admin",
     *   "password": "password123"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Đăng nhập thành công",
     *   "data": {
     *     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "tokenType": "Bearer",
     *     "userId": 1,
     *     "username": "admin",
     *     "fullName": "Nguyễn Văn Admin",
     *     "email": "admin@hcmunre.edu.vn",
     *     "roleName": "ADMIN",
     *     "roleDescription": "Quản trị viên hệ thống"
     *   },
     *   "timestamp": "2025-01-15T10:30:00"
     * }
     * 
     * @param loginRequest DTO chứa username và password
     * @return ResponseEntity với LoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        
        logger.info("API /api/auth/login được gọi với username: {}", loginRequest.getUsername());
        
        try {
            // Gọi AuthService để xử lý login
            LoginResponse loginResponse = authService.login(loginRequest);
            
            // Trả về response thành công
            return ResponseEntity.ok(
                    ApiResponse.success("Đăng nhập thành công", loginResponse)
            );
            
        } catch (BadCredentialsException e) {
            // Username hoặc password sai
            logger.warn("Login thất bại: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Username hoặc password không đúng"));
                    
        } catch (Exception e) {
            // Lỗi khác
            logger.error("Lỗi khi xử lý login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    
    /**
     * Logout endpoint
     * 
     * POST /api/auth/logout
     * Header: Authorization: Bearer <token>
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Đăng xuất thành công",
     *   "data": null,
     *   "timestamp": "2025-01-15T10:35:00"
     * }
     * 
     * @return ResponseEntity
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        logger.info("API /api/auth/logout được gọi");
        
        try {
            authService.logout();
            return ResponseEntity.ok(
                    ApiResponse.success("Đăng xuất thành công", null)
            );
        } catch (Exception e) {
            logger.error("Lỗi khi logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi đăng xuất"));
        }
    }
    
    /**
     * Get current user info
     * 
     * GET /api/auth/me
     * Header: Authorization: Bearer <token>
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "Lấy thông tin user thành công",
     *   "data": {
     *     "userId": 1,
     *     "username": "admin",
     *     "fullName": "Nguyễn Văn Admin",
     *     "email": "admin@hcmunre.edu.vn",
     *     "roleName": "ADMIN",
     *     ...
     *   },
     *   "timestamp": "2025-01-15T10:40:00"
     * }
     * 
     * @return ResponseEntity với UserDTO
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        logger.info("API /api/auth/me được gọi");
        
        try {
            // Lấy username từ SecurityContext (đã set bởi JwtFilter)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Lấy thông tin user
            UserDTO userDTO = userService.getUserInfo(username);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Lấy thông tin user thành công", userDTO)
            );
            
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông tin user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    
    /**
     * Health check endpoint
     * 
     * GET /api/auth/health
     * 
     * Response:
     * {
     *   "success": true,
     *   "message": "API đang hoạt động",
     *   "data": "OK",
     *   "timestamp": "2025-01-15T10:45:00"
     * }
     * 
     * @return ResponseEntity
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("API đang hoạt động", "OK")
        );
    }
}