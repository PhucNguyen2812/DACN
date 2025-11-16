package com.DACN.quanlikhoa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ với bảng "users" trong database
 * 
 * Bảng này lưu thông tin chung của tất cả người dùng trong hệ thống
 * (Admin, Giảng viên, Sinh viên, Giáo vụ...)
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * ID tự tăng - Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    
    /**
     * Tên đăng nhập (UNIQUE)
     * Ví dụ: 
     * - Admin: "admin"
     * - Giảng viên: "giangvien"
     * - Sinh viên: "1150080071" (mã sinh viên)
     */
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    /**
     * Mật khẩu đã được mã hóa bằng BCrypt
     * 
     * ⚠️ QUAN TRỌNG: 
     * - KHÔNG BAO GIỜ lưu plain text password
     * - Phải hash bằng BCryptPasswordEncoder trước khi lưu
     * - Hash của "password123" là: 
     *   $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    /**
     * Email (UNIQUE)
     */
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    /**
     * Số điện thoại
     */
    @Column(name = "phone", length = 20)
    private String phone;
    
    /**
     * Họ và tên đầy đủ
     */
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;
    
    /**
     * Đường dẫn ảnh đại diện
     * Lưu URL hoặc path đến file storage
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    /**
     * Quan hệ Many-to-One với Role
     * 
     * - Nhiều users có thể có cùng một role
     * - Một user chỉ có một role
     * - EAGER: Load role ngay khi load user (cần cho authentication)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    /**
     * Trạng thái tài khoản
     * - true: Active (có thể đăng nhập)
     * - false: Inactive/Locked (không thể đăng nhập)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /**
     * Thời gian đăng nhập lần cuối
     * Update sau mỗi lần login thành công
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    /**
     * Thời gian tạo tài khoản
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật tài khoản
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Callback: Tự động set timestamps khi tạo mới
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Default is_active = true nếu chưa set
        if (isActive == null) {
            isActive = true;
        }
    }
    
    /**
     * Callback: Tự động update timestamp khi cập nhật
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Helper method: Update last_login timestamp
     * Gọi sau khi login thành công
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    /**
     * Check xem user có active không
     * @return true nếu tài khoản đang active
     */
    public boolean isAccountActive() {
        return this.isActive != null && this.isActive;
    }
}