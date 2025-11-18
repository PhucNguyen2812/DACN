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
 * Repository cho entity User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    
    // CUSTOM QUERIES CHO ADMIN
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> findBySearch(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "u.role.roleId = :roleId")
    Page<User> findBySearchAndRole(@Param("search") String search, 
                                    @Param("roleId") Integer roleId, 
                                    Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "u.isActive = :isActive")
    Page<User> findBySearchAndStatus(@Param("search") String search, 
                                      @Param("isActive") Boolean isActive, 
                                      Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "u.role.roleId = :roleId AND " +
           "u.isActive = :isActive")
    Page<User> findBySearchAndRoleAndStatus(@Param("search") String search, 
                                             @Param("roleId") Integer roleId, 
                                             @Param("isActive") Boolean isActive, 
                                             Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role.roleId = :roleId")
    Page<User> findByRoleId(@Param("roleId") Integer roleId, Pageable pageable);
    
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role.roleId = :roleId AND u.isActive = :isActive")
    Page<User> findByRoleIdAndIsActive(@Param("roleId") Integer roleId, 
                                        @Param("isActive") Boolean isActive, 
                                        Pageable pageable);
}