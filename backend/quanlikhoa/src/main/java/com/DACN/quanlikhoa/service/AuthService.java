package com.DACN.quanlikhoa.service;

import com.DACN.quanlikhoa.dto.LoginRequest;
import com.DACN.quanlikhoa.dto.LoginResponse;
import com.DACN.quanlikhoa.entity.User;
import com.DACN.quanlikhoa.repository.UserRepository;
import com.DACN.quanlikhoa.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * 
 * File: AuthService.java
 * Location: src/main/java/com/DACN/quanlikhoa/service/AuthService.java
 * 
 * Service xử lý authentication (đăng nhập)
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Xử lý login
     * 
     * Flow:
     * 1. Authenticate user với username/password
     * 2. Generate JWT token
     * 3. Update last_login
     * 4. Return LoginResponse với token và user info
     * 
     * @param loginRequest DTO chứa username và password
     * @return LoginResponse với token và user info
     * @throws BadCredentialsException nếu username/password sai
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            logger.info("Đang xử lý login cho user: {}", loginRequest.getUsername());
            
            // 1. Authenticate user
            // AuthenticationManager sẽ:
            // - Gọi CustomUserDetailsService.loadUserByUsername()
            // - So sánh password bằng BCryptPasswordEncoder
            // - Nếu đúng → return Authentication object
            // - Nếu sai → throw BadCredentialsException
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            // 2. Set authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 3. Generate JWT token
            String jwt = tokenProvider.generateToken(authentication);
            
            // 4. Load user từ database để lấy thông tin đầy đủ
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User không tồn tại"));
            
            // 5. Update last_login timestamp
            user.updateLastLogin();
            userRepository.save(user);
            
            // 6. Build LoginResponse
            LoginResponse response = LoginResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .roleName(user.getRole().getRoleName())
                    .roleDescription(user.getRole().getRoleDescription())
                    .build();
            
            logger.info("Login thành công cho user: {}", loginRequest.getUsername());
            return response;
            
        } catch (BadCredentialsException e) {
            logger.error("Login thất bại: Username hoặc password không đúng");
            throw new BadCredentialsException("Username hoặc password không đúng");
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý login: {}", e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra khi đăng nhập: " + e.getMessage());
        }
    }
    
    /**
     * Logout (optional - JWT là stateless)
     * 
     * Với JWT stateless, không cần xử lý logout ở server
     * Client chỉ cần xóa token ở local storage
     * 
     * Nếu muốn implement logout nghiêm ngặt:
     * - Lưu token vào blacklist (Redis)
     * - Check blacklist mỗi request
     */
    public void logout() {
        SecurityContextHolder.clearContext();
        logger.info("User đã logout");
    }
    
    /**
     * Get current authenticated user
     * 
     * @return User hiện tại đang đăng nhập
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }
}