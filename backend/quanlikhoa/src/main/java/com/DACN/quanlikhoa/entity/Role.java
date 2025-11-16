package com.DACN.quanlikhoa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ với bảng "roles" trong database
 * 
 * Bảng này lưu trữ các vai trò trong hệ thống:
 * - ADMIN: Quản trị viên (priority 1)
 * - TRUONG_KHOA: Trưởng khoa (priority 2)
 * - PHO_KHOA: Phó khoa (priority 3)
 * - TRUONG_BO_MON: Trưởng bộ môn (priority 3)
 * - GIANG_VIEN: Giảng viên (priority 4)
 * - GIAO_VU: Giáo vụ (priority 5)
 * - SINH_VIEN: Sinh viên (priority 6)
 */
@Entity
@Table(name = "roles")
@Data                    // Lombok: tự tạo getter, setter, toString, equals, hashCode
@NoArgsConstructor       // Lombok: constructor không tham số
@AllArgsConstructor      // Lombok: constructor đầy đủ tham số
public class Role {
    
    /**
     * ID tự tăng (SERIAL trong PostgreSQL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;
    
    /**
     * Tên vai trò (UNIQUE)
     * Ví dụ: "ADMIN", "TRUONG_KHOA", "SINH_VIEN"
     */
    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    private String roleName;
    
    /**
     * Mô tả vai trò
     */
    @Column(name = "role_description", columnDefinition = "TEXT")
    private String roleDescription;
    
    /**
     * Mức độ ưu tiên (số càng nhỏ = quyền càng cao)
     * 1 = Admin, 2 = Trưởng khoa, ..., 6 = Sinh viên
     */
    @Column(name = "priority_level", nullable = false)
    private Integer priorityLevel;
    
    /**
     * Thời gian tạo (tự động)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật (tự động)
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Tự động set thời gian khi tạo mới
     * Được gọi trước khi INSERT vào database
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Tự động update thời gian khi cập nhật
     * Được gọi trước khi UPDATE trong database
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}