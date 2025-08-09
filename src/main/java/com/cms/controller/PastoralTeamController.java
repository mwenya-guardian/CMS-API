package com.cms.controller;

import com.cms.dto.response.ApiResponse;
import com.cms.model.PastoralTeam;
import com.cms.service.PastoralTeamService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pastoral-team")
@AllArgsConstructor
public class PastoralTeamController {

    private final PastoralTeamService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PastoralTeam>>> getAll() {
        List<PastoralTeam> list = service.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PastoralTeam>>> getAllActive() {
        List<PastoralTeam> list = service.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<PastoralTeam>>> getActivePaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PastoralTeam> pg = service.getActivePaginated(page, size);
        return ResponseEntity.ok(ApiResponse.success(pg));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PastoralTeam>> getById(@PathVariable String id) {
        PastoralTeam member = service.getById(id)
                .orElseThrow(() -> new RuntimeException("pastoral team member not found"));
        return ResponseEntity.ok(ApiResponse.success(member));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PastoralTeam>> create(
            @Valid @RequestBody PastoralTeam request
    ) {
        PastoralTeam created = service.create(request);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PastoralTeam>> update(
            @PathVariable String id,
            @Valid @RequestBody PastoralTeam request
    ) {
        PastoralTeam updated = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
