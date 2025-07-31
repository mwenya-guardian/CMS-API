package com.cms.controller;

import com.cms.dto.request.EventRequest;
import com.cms.dto.request.ExportRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.FileUploadResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.Event;
import com.cms.service.EventService;
import com.cms.service.ExportService;
import com.cms.service.FileService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/events")
@AllArgsConstructor
public class EventController {
    private EventService eventService;
    private FileService fileService;
    private ExportService exportService;
    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> getAllEvents(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search) {
        
        List<Event> events = eventService.getAllEvents(year, month, day, category, featured, search);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PageResponse<Event>>> getEventsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search) {
        
        PageResponse<Event> response = eventService.getEventsPaginated(
                page, limit, year, month, day, category, featured, search);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> getEventById(@PathVariable String id) {
        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return ResponseEntity.ok(ApiResponse.success(event));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Event>> createEvent(@Valid @RequestBody EventRequest request) {
        Event event = eventService.createEvent(request);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> updateEvent(
            @PathVariable String id, @Valid @RequestBody EventRequest request) {
        Event event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportEventsToPdf(@Valid @RequestBody ExportRequest request) throws IOException {
        if (!"pdf".equals(request.getFormat())) {
            throw new RuntimeException("Invalid export format. Must be 'pdf'");
        }
        
        List<Event> events = eventService.getEventsByIds(request.getItems());
        if (events.isEmpty()) {
            throw new RuntimeException("No events found for export");
        }
        
        byte[] pdfBytes = exportService.exportEventsToPdf(events, request.getTitle());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "events-export.pdf");
        headers.setCacheControl("no-cache");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        FileUploadResponse response = fileService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}