package com.cms.controller;

import com.cms.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/uploads/private")
@AllArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/**")
    public ResponseEntity<Resource> serveProtectedFile(
            HttpServletRequest request // e.g. protected/photos/2025/uuid.jpg
    ) {
        String requestPath = request.getRequestURI().substring("/api/uploads/private".length());
        Resource resource = fileService.loadAsResource(requestPath);

        // determine content type
        String contentType = null;
        try {
            Path p = Paths.get(resource.getFile().getAbsolutePath());
            contentType = Files.probeContentType(p);
        } catch (IOException ignored) { /* fallback below */ }

        if (contentType == null) {
            contentType = URLConnection.guessContentTypeFromName(resource.getFilename());
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        // inline so images/videos can render in-browser; change to attachment for forcing download
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
