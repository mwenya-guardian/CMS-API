package com.cms.controller;

import com.cms.dto.response.ApiResponse;
import com.cms.model.TitheAndOffering;
import com.cms.service.TitheAndOfferingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("giving")
@AllArgsConstructor
public class TitheAndOfferingController {
    private final TitheAndOfferingService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TitheAndOffering>>> getAll() {
        List<TitheAndOffering> list = service.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<TitheAndOffering>>> getPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TitheAndOffering> pg = service.getPaginated(page, size);
        return ResponseEntity.ok(ApiResponse.success(pg));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TitheAndOffering>> getById(@PathVariable String id) {
        TitheAndOffering member = service.getTitheAndOfferingById(id)
                .orElseThrow(() -> new RuntimeException("pastoral team member not found"));
        return ResponseEntity.ok(ApiResponse.success(member));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TitheAndOffering>> create(
            @Valid @RequestBody TitheAndOffering request
    ) {
        TitheAndOffering created = service.create(request);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TitheAndOffering>> update(
            @PathVariable String id,
            @Valid @RequestBody TitheAndOffering request
    ) {
        TitheAndOffering updated = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
