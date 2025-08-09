package com.cms.controller;

import com.cms.dto.request.BulletinRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.Bulletin;
import com.cms.model.Bulletin.PublicationStatus;
import com.cms.service.BulletinService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bulletins")
@AllArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;

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

    @PostMapping
    public ResponseEntity<ApiResponse<Bulletin>> createBulletin(
            @Valid @RequestBody BulletinRequest request
    ) {
        Bulletin b = bulletinService.createBulletin(request);
        return ResponseEntity.ok(ApiResponse.success(b));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Bulletin>> updateBulletin(
            @PathVariable String id,
            @Valid @RequestBody BulletinRequest request
    ) {
        Bulletin b = bulletinService.updateBulletin(id, request);
        return ResponseEntity.ok(ApiResponse.success(b));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Bulletin>> updatePublishStatus(
            @PathVariable String id
    ) {
        Bulletin b = bulletinService.updateStatus(id, PublicationStatus.PUBLISHED);
        return ResponseEntity.ok(ApiResponse.success(b));
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<Bulletin>> updateUnpublishStatus(
            @PathVariable String id
    ) {
        Bulletin b = bulletinService.updateStatus(id, PublicationStatus.DRAFT);
        return ResponseEntity.ok(ApiResponse.success(b));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBulletin(@PathVariable String id) {
        bulletinService.deleteBulletin(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<Bulletin>>> getBulletinsByIds(
            @RequestBody List<String> ids
    ) {
        List<Bulletin> items = bulletinService.getBulletinsByIds(ids);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}
