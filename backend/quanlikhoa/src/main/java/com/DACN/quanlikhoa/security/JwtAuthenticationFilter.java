package com.DACN.quanlikhoa.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * File: JwtAuthenticationFilter.java
 * Location: src/main/java/com/DACN/quanlikhoa/security/JwtAuthenticationFilter.java
 * 
 * Filter này chạy trước mỗi request để:
 * 1. Lấy JWT token từ header "Authorization"
 * 2. Validate token
 * 3. Load user từ database
 * 4. Set authentication vào SecurityContext
 * 
 * Luồng hoạt động:
 * Client Request → JwtAuthenticationFilter → Controller
 *   ↓
 * Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *   ↓
 * Extract token → Validate → Load user → Set authentication
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * Filter method - được gọi cho mỗi request
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain để chuyển sang filter tiếp theo
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Lấy JWT token từ request header
            String jwt = getJwtFromRequest(request);
            
            // 2. Validate token và lấy username
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Lấy username từ token
                String username = tokenProvider.getUsernameFromToken(jwt);
                
                // 3. Load user details từ database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 4. Tạo authentication object
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 5. Set authentication vào SecurityContext
                // Từ đây, Spring Security biết user đã authenticated
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("Set authentication cho user: {}", username);
            }
        } catch (Exception ex) {
            logger.error("Không thể set user authentication: {}", ex.getMessage());
        }
        
        // 6. Tiếp tục xử lý request
        filterChain.doFilter(request, response);
    }
    
    /**
     * Lấy JWT token từ request header
     * 
     * Header format: Authorization: Bearer <token>
     * 
     * @param request HTTP request
     * @return JWT token string (không có prefix "Bearer ")
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Kiểm tra header có format đúng: "Bearer <token>"
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Cắt bỏ "Bearer " để lấy token
            return bearerToken.substring(7);
        }
        
        return null;
    }
}