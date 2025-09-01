package com.cms.controller;

import com.cms.dto.request.ExportRequest;
import com.cms.dto.request.QuoteRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.FileUploadResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.Quote;
import com.cms.service.ExportService;
import com.cms.service.FileService;
import com.cms.service.QuoteService;
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
@RequestMapping("/quotes")
@AllArgsConstructor
public class QuoteController {
    private QuoteService quoteService;
    private FileService fileService;
    private ExportService exportService;
    
    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Quote>>> getAllQuotes(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search) {
        
        List<Quote> quotes = quoteService.getAllQuotes(year, month, day, category, featured, search);
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<PageResponse<Quote>>> getQuotesPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search) {
        
        PageResponse<Quote> response = quoteService.getQuotesPaginated(
                page, limit, year, month, day, category, featured, search);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Quote>> getQuoteById(@PathVariable String id) {
        Quote quote = quoteService.getQuoteById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        return ResponseEntity.ok(ApiResponse.success(quote));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Quote>> createQuote(@Valid @RequestBody QuoteRequest request) {
        Quote quote = quoteService.createQuote(request);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<ApiResponse<Quote>> updateQuote(
            @PathVariable String id, @Valid @RequestBody QuoteRequest request) {
        Quote quote = quoteService.updateQuote(id, request);
        return ResponseEntity.ok(ApiResponse.success(quote));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Void> deleteQuote(@PathVariable String id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportQuotesToPdf(@Valid @RequestBody ExportRequest request) throws IOException {
        if (!"pdf".equals(request.getFormat())) {
            throw new RuntimeException("Invalid export format. Must be 'pdf'");
        }
        
        List<Quote> quotes = quoteService.getQuotesByIds(request.getItems());
        if (quotes.isEmpty()) {
            throw new RuntimeException("No quotes found for export");
        }
        
        byte[] pdfBytes = exportService.exportQuotesToPdf(quotes, request.getTitle());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "quotes-export.pdf");
        headers.setCacheControl("no-cache");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR)")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
        FileUploadResponse response = fileService.uploadImage(file, true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}