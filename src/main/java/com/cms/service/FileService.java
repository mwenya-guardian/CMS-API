package com.cms.service;

import com.cms.dto.response.FileUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    @Value("${file.upload.max-size}")
    private long maxFileSize;
    
    @Value("${file.upload.allowed-types}")
    private String allowedTypesStr;
    
    @Value("${server.port}")
    private String serverPort;
    
    public FileUploadResponse uploadImage(MultipartFile file) throws IOException {
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Generate URL
        String fileUrl = String.format("http://localhost:%s/api/uploads/%s", serverPort, uniqueFilename);
        
        return new FileUploadResponse(
            fileUrl,
            originalFilename,
            file.getSize(),
            file.getContentType()
        );
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("No file provided");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum limit of 10MB");
        }
        
        List<String> allowedTypes = Arrays.asList(allowedTypesStr.split(","));
        if (!allowedTypes.contains(file.getContentType())) {
            throw new RuntimeException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }
}