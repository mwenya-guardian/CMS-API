package com.cms.dto.response;

import java.util.List;

public class PageResponse<T> {
    private List<T> data;
    private PaginationInfo pagination;
    
    // Constructors
    public PageResponse() {}
    
    public PageResponse(List<T> data, int page, int limit, long total) {
        this.data = data;
        this.pagination = new PaginationInfo(page, limit, total);
    }
    
    // Inner class for pagination info
    public static class PaginationInfo {
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
        
        // Getters and Setters
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
    
    // Getters and Setters
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    
    public PaginationInfo getPagination() { return pagination; }
    public void setPagination(PaginationInfo pagination) { this.pagination = pagination; }
}