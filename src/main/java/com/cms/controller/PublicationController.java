package com.cms.controller;

import com.cms.dto.request.ExportRequest;
import com.cms.dto.request.PublicationRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.FileUploadResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.Publication;
import com.cms.service.ExportService;
import com.cms.service.FileService;
import com.cms.service.PublicationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/publications")
@AllArgsConstructor
public class PublicationController {
    private PublicationService publicationService;
    private FileService fileService;
    private ExportService exportService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Publication>>> getAllPublications(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search) {
        
        List<Publication> publications = publicationService.getAllPublications(
                year, month, day, category, featured, search);
        return ResponseEntity.ok(ApiResponse.success(publications));
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PageResponse<Publication>>> getPublicationsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search) {
        
        PageResponse<Publication> response = publicationService.getPublicationsPaginated(
                page, limit, year, month, day, category, featured, search);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Publication>> getPublicationById(@PathVariable String id) {
        Publication publication = publicationService.getPublicationById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        return ResponseEntity.ok(ApiResponse.success(publication));
    }
    
    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<List<Publication>>> getPublicationsByYear(@PathVariable int year) {
        List<Publication> publications = publicationService.getPublicationsByYear(year);
        return ResponseEntity.ok(ApiResponse.success(publications));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Publication>> createPublication(@Valid @RequestBody PublicationRequest request) {
        Publication publication = publicationService.createPublication(request);
        return ResponseEntity.ok(ApiResponse.success(publication));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Publication>> updatePublication(
            @PathVariable String id, @Valid @RequestBody PublicationRequest request) {
        Publication publication = publicationService.updatePublication(id, request);
        return ResponseEntity.ok(ApiResponse.success(publication));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Void> deletePublication(@PathVariable String id) {
        publicationService.deletePublication(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPublicationsToPdf(@Valid @RequestBody ExportRequest request) throws IOException {
        if (!"pdf".equals(request.getFormat())) {
            throw new RuntimeException("Invalid export format. Must be 'pdf'");
        }
        
        List<Publication> publications = publicationService.getPublicationsByIds(request.getItems());
        if (publications.isEmpty()) {
            throw new RuntimeException("No publications found for export");
        }
        
        byte[] pdfBytes = exportService.exportPublicationsToPdf(publications, request.getTitle());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "publications-export.pdf");
        headers.setCacheControl("no-cache");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @PostMapping("/export/ppt")
    public ResponseEntity<byte[]> exportPublicationsToPpt(@Valid @RequestBody ExportRequest request) throws IOException {
        if (!"ppt".equals(request.getFormat())) {
            throw new RuntimeException("Invalid export format. Must be 'ppt'");
        }
        
        List<Publication> publications = publicationService.getPublicationsByIds(request.getItems());
        if (publications.isEmpty()) {
            throw new RuntimeException("No publications found for export");
        }
        
        byte[] pptBytes = exportService.exportPublicationsToPpt(publications, request.getTitle());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        headers.setContentDispositionFormData("attachment", "publications-export.pptx");
        headers.setCacheControl("no-cache");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pptBytes);
    }
    
    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        FileUploadResponse response = fileService.uploadImage(file, true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}