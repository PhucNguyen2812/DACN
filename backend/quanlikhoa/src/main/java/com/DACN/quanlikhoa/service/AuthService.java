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
    
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            logger.info("Đang xử lý login cho user: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String jwt = tokenProvider.generateToken(authentication);
            
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User không tồn tại"));
            
            user.updateLastLogin();
            userRepository.save(user);
            
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
    
    public void logout() {
        SecurityContextHolder.clearContext();
        logger.info("User đã logout");
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }
}