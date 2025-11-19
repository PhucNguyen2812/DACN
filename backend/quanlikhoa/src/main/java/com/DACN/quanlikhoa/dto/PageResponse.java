package com.DACN.quanlikhoa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic PageResponse DTO for paginated data
 * 
 * File: PageResponse.java
 * Location: src/main/java/com/DACN/quanlikhoa/dto/PageResponse.java
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    
    /**
     * Danh sách items trong trang hiện tại
     */
    private List<T> content;
    
    /**
     * Số trang hiện tại (bắt đầu từ 0)
     */
    private Integer currentPage;
    
    /**
     * Tổng số trang
     */
    private Integer totalPages;
    
    /**
     * Tổng số records trong database
     */
    private Long totalElements;
    
    /**
     * Số items mỗi trang
     */
    private Integer pageSize;
    
    /**
     * Có trang tiếp theo không
     */
    private Boolean hasNext;
    
    /**
     * Có trang trước đó không
     */
    private Boolean hasPrevious;
}