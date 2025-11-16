package com.DACN.quanlikhoa.security;

import com.DACN.quanlikhoa.entity.User;
import com.DACN.quanlikhoa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom UserDetailsService implementation
 * 
 * File: CustomUserDetailsService.java
 * Location: src/main/java/com/DACN/quanlikhoa/security/CustomUserDetailsService.java
 * 
 * Service này implement UserDetailsService của Spring Security
 * Nhiệm vụ: Load user từ database và convert sang UserDetails object
 * 
 * Spring Security sẽ dùng UserDetails để:
 * - Authenticate user (check username/password)
 * - Authorize user (check authorities/roles)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Load user by username
     * 
     * Method này được Spring Security gọi khi:
     * 1. User login (AuthenticationManager gọi)
     * 2. JWT filter load user từ token
     * 
     * @param username Username (có thể là username hoặc email)
     * @return UserDetails object chứa thông tin user và authorities
     * @throws UsernameNotFoundException nếu user không tồn tại
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm user trong database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> 
                        new UsernameNotFoundException("User không tồn tại với username: " + username));
        
        // 2. Kiểm tra tài khoản có active không
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + username);
        }
        
        // 3. Convert sang UserDetails
        return buildUserDetails(user);
    }
    
    /**
     * Load user by ID
     * 
     * @param userId User ID
     * @return UserDetails object
     * @throws UsernameNotFoundException nếu user không tồn tại
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Integer userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> 
                        new UsernameNotFoundException("User không tồn tại với ID: " + userId));
        
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa với ID: " + userId);
        }
        
        return buildUserDetails(user);
    }
    
    /**
     * Build UserDetails object từ User entity
     * 
     * UserDetails bao gồm:
     * - username: Tên đăng nhập
     * - password: Mật khẩu đã hash
     * - authorities: Danh sách quyền (roles)
     * - accountNonExpired, accountNonLocked, credentialsNonExpired, enabled
     * 
     * @param user User entity từ database
     * @return UserDetails object
     */
    private UserDetails buildUserDetails(User user) {
        // Tạo authorities từ role
        // Format: ROLE_<roleName>
        // Ví dụ: ROLE_ADMIN, ROLE_SINH_VIEN
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
        
        // Trả về UserDetails với:
        // - username
        // - password (đã hash)
        // - authorities (roles)
        // - các flag (enabled, accountNonExpired, ...)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getIsActive())  // Locked nếu isActive = false
                .credentialsExpired(false)
                .disabled(!user.getIsActive())       // Disabled nếu isActive = false
                .build();
    }
}