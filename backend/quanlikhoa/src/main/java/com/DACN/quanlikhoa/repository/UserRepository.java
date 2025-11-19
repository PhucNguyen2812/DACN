package com.DACN.quanlikhoa.repository;

import com.DACN.quanlikhoa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho User Entity
 * 
 * File: UserRepository.java
 * Location: src/main/java/com/DACN/quanlikhoa/repository/UserRepository.java
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // ===== QUERIES CHO AUTHENTICATION (KHÔNG ĐỘNG VÀO) =====
    
    /**
     * Tìm user theo username (cho login)
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Kiểm tra username đã tồn tại
     */
    boolean existsByUsername(String username);
    
    /**
     * Kiểm tra email đã tồn tại
     */
    boolean existsByEmail(String email);
    
    // ===== QUERIES CHO ADMIN CRUD =====
    
    /**
     * Tìm tất cả users với phân trang
     */
    @Override
    Page<User> findAll(Pageable pageable);
    
    /**
     * Tìm theo roleId và isActive
     */
    @Query("SELECT u FROM User u WHERE u.role.roleId = :roleId AND u.isActive = :isActive")
    Page<User> findByRoleIdAndIsActive(
            @Param("roleId") Integer roleId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
    
    /**
     * Tìm theo roleId
     */
    @Query("SELECT u FROM User u WHERE u.role.roleId = :roleId")
    Page<User> findByRoleId(@Param("roleId") Integer roleId, Pageable pageable);
    
    /**
     * Tìm theo isActive
     */
    @Query("SELECT u FROM User u WHERE u.isActive = :isActive")
    Page<User> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);
    
    /**
     * Tìm kiếm theo keyword (username, email, fullName)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> findBySearch(@Param("search") String search, Pageable pageable);
    
    /**
     * Tìm kiếm + filter theo roleId
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.role.roleId = :roleId")
    Page<User> findBySearchAndRole(
            @Param("search") String search,
            @Param("roleId") Integer roleId,
            Pageable pageable
    );
    
    /**
     * Tìm kiếm + filter theo isActive
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.isActive = :isActive")
    Page<User> findBySearchAndStatus(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
    
    /**
     * Tìm kiếm + filter theo roleId và isActive
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND u.role.roleId = :roleId " +
           "AND u.isActive = :isActive")
    Page<User> findBySearchAndRoleAndStatus(
            @Param("search") String search,
            @Param("roleId") Integer roleId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
    
    /**
     * Đếm số lượng users theo roleId
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.roleId = :roleId")
    Long countByRoleId(@Param("roleId") Integer roleId);
    
    /**
     * Đếm số lượng users active
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
    
    /**
     * Đếm số lượng users inactive
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = false")
    Long countInactiveUsers();
}