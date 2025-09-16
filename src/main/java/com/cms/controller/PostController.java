package com.cms.controller;


import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.Post;
import com.cms.service.PostService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * List posts (paginated)
     * GET /posts?page=1&limit=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Post>>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageResponse<Post> pageResp = postService.postPageResponse(page, limit);
        return ResponseEntity.ok(ApiResponse.success(pageResp));
    }
    /**
     * List posts (paginated) for specific type
     * GET /posts/{type}?page=1&limit=10
     */
    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<PageResponse<Post>>> listPostsByType(
            @PathVariable("type") Post.PostType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageResponse<Post> pageResp = postService.postPageResponse(page, limit, type);
        return ResponseEntity.ok(ApiResponse.success(pageResp));
    }

    /**
     * Get post by id
     * GET /posts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Post>> getById(@PathVariable String id) {
        Optional<Post> p = postService.getById(id);
        return p.map(post -> ResponseEntity.ok(ApiResponse.success(post)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Post not found")));
    }

    /**
     * Create an image post
     * POST /posts/image
     * form-data: caption (string), file (file), isPublic (boolean, optional)
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Post>> createImagePost(
            @RequestPart("caption") String caption,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "isPublic", required = false, defaultValue = "false") boolean isPublic
    ) {
        try {
            Post created = postService.createImagePost(caption, file, isPublic);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create image post: " + ex.getMessage()));
        }
    }

    /**
     * Create a video post.
     * POST /posts/video
     * form-data: caption (string), file (file), isPublic (boolean, optional)
     */
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Post>> createVideoPost(
            @RequestPart("caption") String caption,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "isPublic", required = false, defaultValue = "false") boolean isPublic
    ) {
        try {
            Post created = postService.createVideoPost(caption, file, isPublic);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create video post: " + ex.getMessage()));
        }
    }

    /**
     * Create caption-only post (JSON)
     * POST /posts
     * body: { "caption": "...", "isPublic": true|false }
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Post>> createCaptionPost(
            @RequestBody CaptionRequest request,
            @RequestParam(name = "isPublic", required = false, defaultValue = "false") boolean isPublic
            ) {
        try {
            Post created = postService.createCaptionPost(request.getCaption(), isPublic);
            return ResponseEntity.ok(ApiResponse.success(created));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create caption post: " + ex.getMessage()));
        }
    }

    /**
     * Update caption for a post
     * PUT /posts/{id}/caption
     * body: { "caption": "..." }
     */
    @PutMapping("/{id}/caption")
    public ResponseEntity<ApiResponse<Post>> updateCaption(
            @PathVariable String id,
            @RequestBody CaptionRequest request
    ) {
        try {
            Post updated = postService.updateCaption(id, request.getCaption());
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        }
    }

    /**
     * Delete post
     * DELETE /posts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @Getter
    @Setter
    // Simple request DTOs used by endpoints
    public static class CaptionRequest {
        private String caption;
    }
}
