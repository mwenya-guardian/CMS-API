package com.cms.controller;

import com.cms.dto.response.ApiResponse;
import com.cms.model.Members;
import com.cms.service.MembersService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cms.dto.response.FileUploadResponse;
import com.cms.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MembersController {

    private final MembersService service;
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Members>>> getAll() {
        List<Members> list = service.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<Members>>> getPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Members> pg = service.getPaginated(page, size);
        return ResponseEntity.ok(ApiResponse.success(pg));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Members>> getById(@PathVariable String id) {
        Members member = service.getMembersById(id)
                .orElseThrow(() -> new RuntimeException("pastoral team member not found"));
        return ResponseEntity.ok(ApiResponse.success(member));
    }

    @GetMapping("position/{position}")
    public ResponseEntity<ApiResponse<List<Members>>> getByPosition(@PathVariable String position) {
        List<Members> members = service.getMembersByPosition(position);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @GetMapping("positionType/{positionType}")
    public ResponseEntity<ApiResponse<List<Members>>> getByPositionType(@PathVariable String positionType) {
        List<Members> members = service.getMembersByPositionType(positionType);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Members>> create(
            @Valid @RequestBody Members request
    ) {
        Members created = service.create(request);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Members>> update(
            @PathVariable String id,
            @Valid @RequestBody Members request
    ) throws IOException {
        Members updated = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        FileUploadResponse response = fileService.uploadImage(file, true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
