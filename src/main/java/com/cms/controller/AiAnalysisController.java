package com.cms.controller;

import com.cms.client.GeminiChatService;
import com.cms.dto.request.BulkAnalysisRequest;
import com.cms.dto.request.CommentRequest;
import com.cms.dto.response.BulkAnalysisResponse;
import com.cms.model.ReactionBaseDocument;
import com.cms.service.AiAnalysisService;
import com.cms.repository.AnalysisJobRepository;
import com.cms.model.AnalysisJob;
import com.cms.service.ReactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
@AllArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService analysisService;
    private final GeminiChatService geminiChatService;
    private final AnalysisJobRepository jobRepository;
    // inject commentRepository to load comment list for the entity
    private final ReactionService reactionService;

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam String text){
        return ResponseEntity.ok(geminiChatService.chat(text));
    }

    @PostMapping("/analyze/bulk")
    public ResponseEntity<BulkAnalysisResponse> analyzeBulk(@RequestBody BulkAnalysisRequest request) {
        // load comments belonging to entity (implement your CommentRepository find method)
//        List<CommentRequest> comments = commentRepository.findCommentsByEntity(request.getEntityType(), request.getEntityId(), request.getSince(), request.getMaxComments());
        List<CommentRequest> comments = reactionService
                .findByTypeAndTargetId(ReactionBaseDocument.ReactionType.COMMENT, request.getEntityType(), request.getEntityId())
                .stream().map((comment) ->{
                    return new CommentRequest(comment.getId(), comment.getComment(), comment.getCreatedAt());
                })
                .toList();
        return ResponseEntity.ok(analysisService.submitBulk(request, comments));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<AnalysisJob> getJob(@PathVariable String jobId) {
        return jobRepository.findById(jobId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<AnalysisJob>> listJobs() {
        return ResponseEntity.ok(new ArrayList<>(jobRepository.findAll()));
    }
}
