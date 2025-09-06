package com.cms.controller;

import com.cms.dto.request.ReactionRequest;
import com.cms.dto.request.UpdateReactionRequest;
import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.ReactionResponse;
import com.cms.model.*;
import com.cms.service.ReactionService;
import com.cms.utils.ReactionMappers;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for managing reactions across different categories (POST, PUBLICATION, EVENT, QUOTE).
 *
 * Endpoints:
 *  POST   /reactions/{category}          -> create reaction
 *  GET    /reactions/{category}/{id}     -> read reaction
 *  PUT    /reactions/{category}/{id}     -> update reaction
 *  DELETE /reactions/{category}/{id}     -> delete reaction
 */
@RestController
@RequestMapping("/reactions")
@AllArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;
    private final ReactionMappers reactionMappers;

    /**
     * Create a reaction for a given category.
     */
    @PostMapping("/{category}")
    public ResponseEntity<ApiResponse<ReactionResponse>> createReaction(
            @PathVariable("category") ReactionService.ReactionCategory category,
            @Validated @RequestBody ReactionRequest request
    ) {
        try {
            ReactionBaseDocument toSave = reactionMappers.mapToSpecificReaction(request, category);
            ReactionBaseDocument saved = reactionService.createReaction(toSave, category);

            ReactionResponse resp = reactionMappers.mapToResponse(saved, category);
            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(ApiResponse.error(iae.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create reaction"));
        }
    }

    /**
     * Read reaction by id within a category.
     */
    @GetMapping("/{category}/{id}")
    public ResponseEntity<ApiResponse<ReactionResponse>> getById(
            @PathVariable("category") ReactionService.ReactionCategory category,
            @PathVariable("id") String id
    ) {
        try {
            Optional<? extends ReactionBaseDocument> found = reactionService.findById(id, category);
            return found
                    .map(r -> ResponseEntity.ok(ApiResponse.success(reactionMappers.mapToResponse(r, category))))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Reaction not found")));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(ApiResponse.error(iae.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch reaction"));
        }
    }

    /**
     * Update reaction (replace allowed fields).
     */
    @PutMapping("/{category}/{id}")
    public ResponseEntity<ApiResponse<ReactionResponse>> updateReaction(
            @PathVariable("category") ReactionService.ReactionCategory category,
            @PathVariable("id") String id,
            @Validated @RequestBody UpdateReactionRequest request
    ) {
        try {
            // Load existing
            Optional<? extends ReactionBaseDocument> existingOpt = reactionService.findById(id, category);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Reaction not found"));
            }

            ReactionBaseDocument existing = existingOpt.get();

            // update allowed fields: type and comment
            if (request.getType() != null) {
                existing.setType(request.getType());
            }
            if (request.getComment() != null) {
                existing.setComment(request.getComment());
            }

            ReactionBaseDocument updated = reactionService.updateReaction(id, existing, category);
            return ResponseEntity.ok(ApiResponse.success(reactionMappers.mapToResponse(updated, category)));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(ApiResponse.error(iae.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update reaction"));
        }
    }

    /**
     * Delete reaction by id and category.
     */
    @DeleteMapping("/{category}/{id}")
    public ResponseEntity<Void> deleteReaction(
            @PathVariable("category") ReactionService.ReactionCategory category,
            @PathVariable("id") String id
    ) {
        try {
            reactionService.deleteReaction(id, category);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
