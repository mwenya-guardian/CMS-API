package com.cms.controller;

import com.cms.service.FileService;
import com.cms.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/media")
@AllArgsConstructor
public class MediaController {

//    private final FileService fileService;
    private final PostService postService; // if you want to check ownership/authorization

    @GetMapping("/posts/{postId}/stream")
    public ResponseEntity<?> streamPostMedia(
            @PathVariable String postId,
            @RequestHeader(value = "Range", required = false) String rangeHeader
            ) throws Exception {

        // Authorization check (example)
        // Optional<Post> pOpt = postService.getById(postId);
        // if (pOpt.isEmpty()) return ResponseEntity.notFound().build();
        // if (!canAccess(pOpt.get(), principal)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        // Load relative stored path from Post
        Resource resource = postService.loadMediaForPost(postId);
        long contentLength = resource.contentLength();

        if (rangeHeader == null) {
            // No Range header — serve whole resource
            MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(new InputStreamResource(resource.getInputStream()));
        }

        // Parse single-range (we'll support only the first range)
        List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
        HttpRange range = ranges.getFirst();

        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);
        long rangeLength = end - start + 1;

        ResourceRegion region = new ResourceRegion(resource, start, rangeLength);

        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(mediaType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                .contentLength(rangeLength)
                .body(region);
    }
    @GetMapping("/posts/{postId}/file")
    public ResponseEntity<Void> internalRedirectToFile(@PathVariable String postId) {
        // authorize...
        String storedPath = postService.getById(postId).orElseThrow().getResourceUrl(); // e.g. protected/videos/2025/uuid.mp4
        // map to internal path configured in nginx: /internal_uploads/protected/videos/...
        String internal = "/internal_uploads/" + storedPath;
        return ResponseEntity.status(HttpStatus.OK)
                .header("X-Accel-Redirect", internal)
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .build();
    }

}

