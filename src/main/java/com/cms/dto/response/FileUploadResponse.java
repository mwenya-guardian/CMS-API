package com.cms.dto.response;

public class FileUploadResponse {
    private String url;
    private String filename;
    private long size;
    private String mimetype;
    
    // Constructors
    public FileUploadResponse() {}
    
    public FileUploadResponse(String url, String filename, long size, String mimetype) {
        this.url = url;
        this.filename = filename;
        this.size = size;
        this.mimetype = mimetype;
    }
    
    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public String getMimetype() { return mimetype; }
    public void setMimetype(String mimetype) { this.mimetype = mimetype; }
}