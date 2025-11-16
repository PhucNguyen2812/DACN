package com.DACN.quanlikhoa.repository;

import com.DACN.quanlikhoa.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity Role
 * 
 * JpaRepository cung cấp sẵn các method:
 * - findAll(): Lấy tất cả roles
 * - findById(id): Tìm role theo ID
 * - save(role): Lưu/update role
 * - deleteById(id): Xóa role
 * - count(): Đếm số roles
 * ... và nhiều method khác
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * Tìm role theo tên (ADMIN, TRUONG_KHOA, ...)
     * 
     * Spring Data JPA tự động implement method này
     * dựa vào tên method theo convention:
     * - findBy + FieldName + (Condition)
     * 
     * SQL tương đương:
     * SELECT * FROM roles WHERE role_name = ?
     * 
     * @param roleName Tên role (VD: "ADMIN", "SINH_VIEN")
     * @return Optional<Role> - có thể empty nếu không tìm thấy
     */
    Optional<Role> findByRoleName(String roleName);
    
    /**
     * Kiểm tra role có tồn tại không
     * 
     * SQL tương đương:
     * SELECT EXISTS(SELECT 1 FROM roles WHERE role_name = ?)
     * 
     * @param roleName Tên role
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByRoleName(String roleName);
}