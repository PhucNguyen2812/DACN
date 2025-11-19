package com.DACN.quanlikhoa.config;

import com.DACN.quanlikhoa.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration
 * 
 * File: SecurityConfig.java
 * Location: src/main/java/com/DACN/quanlikhoa/config/SecurityConfig.java
 * 
 * Mô tả: Cấu hình bảo mật cho ứng dụng
 * - JWT Token-based authentication
 * - Password encoding với BCrypt
 * - Role-based access control
 * - CORS configuration
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * Password Encoder Bean - BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Authentication Manager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
    
    /**
     * Security Filter Chain - Main security configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (vì dùng JWT token)
            .csrf().disable()
            
            // Enable CORS
            .cors()
            .and()
            
            // Exception handling
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(401);
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                })
                .and()
            
            // Session management
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            
            // Authorization configuration
            .authorizeHttpRequests()
                // Public endpoints (không cần xác thực)
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Admin endpoints (chỉ ADMIN và TRUONG_KHOA)
                .requestMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers(HttpMethod.POST, "/admin/**").hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers(HttpMethod.PUT, "/admin/**").hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers(HttpMethod.DELETE, "/admin/**").hasAnyRole("ADMIN", "TRUONG_KHOA")
                
                // Tất cả request khác cần xác thực
                .anyRequest().authenticated()
                .and()
            
            // Thêm JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}