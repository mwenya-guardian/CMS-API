package com.cms.controller;

import com.cms.dto.request.BulletinRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.BulletinSummary;
import com.cms.dto.response.PageResponse;
import com.cms.model.Bulletin;
import com.cms.model.Bulletin.PublicationStatus;
import com.cms.service.BulletinService;
import com.cms.service.ExportService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bulletins")
@AllArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;
    private final ExportService exportService;
    @GetMapping
    public ResponseEntity<ApiResponse<List<Bulletin>>> getAllBulletins(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) PublicationStatus status,
            @RequestParam(required = false) String authorId,
            @RequestParam(required = false) String search
    ) {
        List<Bulletin> items = bulletinService.getAllBulletins(date, status, authorId, search);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PageResponse<Bulletin>>> getBulletinsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) PublicationStatus status,
            @RequestParam(required = false) String authorId,
            @RequestParam(required = false) String search
    ) {
        PageResponse<Bulletin> response = bulletinService.getBulletinsPaginated(
                page, limit, date, status, authorId, search
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Bulletin>> getBulletinById(@PathVariable String id) {
        Bulletin b = bulletinService.getBulletinById(id)
                .orElseThrow(() -> new RuntimeException("bulletin not found"));
        return ResponseEntity.ok(ApiResponse.success(b));
    }

    @GetMapping("/published-summaries")
    public ResponseEntity<ApiResponse<List<BulletinSummary>>> getPublishedBulletinSummaries() {
        List<BulletinSummary> summaries = bulletinService.getPublishedBulletinSummaries();
        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Bulletin>> createBulletin(
            @Valid @RequestBody BulletinRequest request
    ) {
        Bulletin b = bulletinService.createBulletin(request);
        return ResponseEntity.ok(ApiResponse.success(b));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Bulletin>> updateBulletin(
            @PathVariable String id,
            @Valid @RequestBody BulletinRequest request
    ) {
        Bulletin b = bulletinService.updateBulletin(id, request);
        return ResponseEntity.ok(ApiResponse.success(b));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Bulletin>> updatePublishStatus(
            @PathVariable String id
    ) {
        Bulletin b = bulletinService.updateStatus(id, PublicationStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success(b));
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Bulletin>> updateUnpublishStatus(
            @PathVariable String id
    ) {
        Bulletin b = bulletinService.updateStatus(id, PublicationStatus.DRAFT);
        return ResponseEntity.ok(ApiResponse.success(b));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBulletin(@PathVariable String id) {
        bulletinService.deleteBulletin(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Bulletin>>> getBulletinsByIds(
            @RequestBody List<String> ids

    ) {
        List<Bulletin> items = bulletinService.getBulletinsByIds(ids);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
    
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getTotalCount() {
        long count = bulletinService.getTotalCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @GetMapping("/count/year/{year}")
    public ResponseEntity<ApiResponse<Long>> getCountByYear(@PathVariable int year) {
        long count = bulletinService.getCountByYear(year);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @GetMapping("/count/published")
    public ResponseEntity<ApiResponse<Long>> getPublishedCount() {
        long count = bulletinService.getPublishedCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportBulletinToPdf(@PathVariable String id) throws IOException {
        Bulletin bulletin = bulletinService.getBulletinById(id)
                .orElseThrow(() -> new RuntimeException("Bulletin not found"));
        
        byte[] pdfBytes = exportService.exportBulletinToPdf(bulletin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bulletin-" + id + ".pdf");
        headers.setCacheControl("no-cache");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @GetMapping("/{id}/export/word")
    public ResponseEntity<byte[]> exportBulletinToWord(@PathVariable String id) throws IOException {
        Bulletin bulletin = bulletinService.getBulletinById(id)
                .orElseThrow(() -> new RuntimeException("Bulletin not found"));
        
        byte[] wordBytes = exportService.exportBulletinToWord(bulletin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDispositionFormData("attachment", "bulletin-" + id + ".docx");
        headers.setCacheControl("no-cache");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(wordBytes);
    }
}
