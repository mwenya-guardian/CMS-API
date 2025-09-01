package com.cms.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PageResponse<T> {
    // Getters and Setters
    private List<T> data;
    private PaginationInfo pagination;
    
    // Constructors
    public PageResponse() {}
    
    public PageResponse(List<T> data, int page, int limit, long total) {
        this.data = data;
        this.pagination = new PaginationInfo(page, limit, total);
    }
    
    // Inner class for pagination info
    @Setter
    @Getter
    public static class PaginationInfo {
        // Getters and Setters
        private int page;
        private int limit;
        private long total;
        private int totalPages;
        
        public PaginationInfo(int page, int limit, long total) {
            this.page = page;
            this.limit = limit;
            this.total = total;
            this.totalPages = (int) Math.ceil((double) total / limit);
        }

    }

}