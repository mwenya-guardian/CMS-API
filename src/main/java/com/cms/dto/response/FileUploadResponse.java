package com.cms.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileUploadResponse {
    // Getters and Setters
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

}