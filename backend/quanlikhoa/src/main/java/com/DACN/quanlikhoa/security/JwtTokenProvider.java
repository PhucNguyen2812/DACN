package com.DACN.quanlikhoa.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Provider để tạo và validate JWT Token
 * 
 * File: JwtTokenProvider.java
 * Location: src/main/java/com/DACN/quanlikhoa/security/JwtTokenProvider.java
 * 
 * Chức năng:
 * 1. Tạo JWT token từ username
 * 2. Validate token
 * 3. Lấy username từ token
 */
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    /**
     * Secret key để sign JWT token
     * Inject từ application.properties: jwt.secret
     */
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    /**
     * Thời gian hết hạn của token (milliseconds)
     * Inject từ application.properties: jwt.expiration
     * Default: 86400000 ms = 24 hours
     */
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    /**
     * Tạo JWT token từ Authentication object
     * 
     * Token structure:
     * Header: {"alg": "HS256", "typ": "JWT"}
     * Payload: {"sub": "admin", "iat": 1234567890, "exp": 1234654290}
     * Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
     * 
     * @param authentication Spring Security Authentication object
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .setSubject(userDetails.getUsername())  // Username trong payload
                .setIssuedAt(now)                       // Thời gian tạo token
                .setExpiration(expiryDate)              // Thời gian hết hạn
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // Sign với secret key
                .compact();
    }
    
    /**
     * Tạo JWT token từ username (overload method)
     * 
     * @param username Username
     * @return JWT token string
     */
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Lấy username từ JWT token
     * 
     * Parse token và extract "sub" claim (subject)
     * 
     * @param token JWT token string
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    /**
     * Validate JWT token
     * 
     * Kiểm tra:
     * 1. Token có đúng format không
     * 2. Signature có hợp lệ không
     * 3. Token có hết hạn chưa
     * 
     * @param authToken JWT token string
     * @return true nếu token hợp lệ, false nếu không
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT không hợp lệ: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT đã hết hạn: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT không được hỗ trợ: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string trống: {}", ex.getMessage());
        } catch (Exception ex) {
            logger.error("Lỗi validate JWT: {}", ex.getMessage());
        }
        return false;
    }
    
    /**
     * Tạo signing key từ secret
     * 
     * Convert secret string thành Key object để sign token
     * 
     * @return Key object
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}