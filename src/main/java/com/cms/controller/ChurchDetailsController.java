package com.cms.controller;

import com.cms.dto.response.ApiResponse;
import com.cms.model.ChurchDetails;
import com.cms.service.ChurchDetailsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/church-details")
@AllArgsConstructor
public class ChurchDetailsController {

    private final ChurchDetailsService churchDetailsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChurchDetails>>> getAll() {
        List<ChurchDetails> list = churchDetailsService.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChurchDetails>> getById(@PathVariable String id) {
        ChurchDetails details = churchDetailsService.getById(id)
                .orElseThrow(() -> new RuntimeException("church details not found"));
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChurchDetails>> create(
            @Valid @RequestBody ChurchDetails request
    ) {
        ChurchDetails created = churchDetailsService.create(request);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChurchDetails>> update(
            @PathVariable String id,
            @Valid @RequestBody ChurchDetails request
    ) {
        ChurchDetails updated = churchDetailsService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        churchDetailsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
