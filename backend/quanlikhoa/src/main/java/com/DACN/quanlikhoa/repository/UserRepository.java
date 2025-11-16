package com.DACN.quanlikhoa.repository;

import com.DACN.quanlikhoa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity User
 * 
 * Cung cấp các method để truy vấn bảng users
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Tìm user theo username
     * 
     * ⚠️ QUAN TRỌNG: Method này dùng cho Authentication
     * 
     * SQL: SELECT * FROM users u 
     *      JOIN roles r ON u.role_id = r.role_id 
     *      WHERE u.username = ?
     * 
     * @param username Tên đăng nhập
     * @return Optional<User> với Role đã được load (EAGER)
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Tìm user theo email
     * 
     * SQL: SELECT * FROM users WHERE email = ?
     * 
     * @param email Email
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Kiểm tra username đã tồn tại chưa
     * 
     * Dùng khi tạo user mới để check duplicate
     * 
     * SQL: SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)
     * 
     * @param username Tên đăng nhập cần check
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByUsername(String username);
    
    /**
     * Kiểm tra email đã tồn tại chưa
     * 
     * SQL: SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     * 
     * @param email Email cần check
     * @return true nếu đã tồn tại, false nếu chưa
     */
    boolean existsByEmail(String email);
    
    /**
     * Tìm user theo username và kiểm tra active
     * 
     * Dùng cho login - chỉ cho phép user active đăng nhập
     * 
     * SQL: SELECT * FROM users 
     *      WHERE username = ? AND is_active = true
     * 
     * @param username Tên đăng nhập
     * @return Optional<User> nếu user tồn tại VÀ đang active
     */
    Optional<User> findByUsernameAndIsActiveTrue(String username);
}