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
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        
        logger.info("API /api/auth/login - username: {}", loginRequest.getUsername());
        
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return ResponseEntity.ok(
                    ApiResponse.success("Đăng nhập thành công", loginResponse)
            );
        } catch (BadCredentialsException e) {
            logger.warn("Login thất bại: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Username hoặc password không đúng"));
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra: " + e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        logger.info("API /api/auth/logout");
        
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
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        logger.info("API /api/auth/me");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
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
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("API đang hoạt động", "OK")
        );
    }
}