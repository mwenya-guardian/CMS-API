package com.cms.controller;

import com.cms.dto.response.ApiResponse;
import com.cms.dto.response.PageResponse;
import com.cms.model.AnalysisAlert;
import com.cms.model.CommentAnalysis;
import com.cms.repository.AnalysisAlertRepository;
import com.cms.repository.CommentAnalysisRepository;
import com.cms.service.ReactionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analysis")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalysisController {

    private final CommentAnalysisRepository analysisRepository;
    private final AnalysisAlertRepository alertRepository;
    private final ReactionService reactionService;

    @Data
    public static class AnalysisStats {
        private int totalAnalyzed;
        private int flaggedComments;
        private SentimentBreakdown sentimentBreakdown;
        private List<AnalysisAlert> recentAlerts;
        private List<TrendData> trendData;

        @Data
        public static class SentimentBreakdown {
            private int positive;
            private int neutral;
            private int negative;
        }

        @Data
        public static class TrendData {
            private String date;
            private int positive;
            private int neutral;
            private int negative;
            private int total;
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AnalysisStats>> getAnalysisStats() {
        // Get total analyzed comments
        long totalAnalyzed = analysisRepository.count();
        
        // Get flagged comments count
        long flaggedComments = analysisRepository.countByModerationFlagged(true);
        
        // Get sentiment breakdown from comment analysis
        long positiveCount = analysisRepository.countBySentiment("positive");
        long neutralCount = analysisRepository.countBySentiment("neutral");
        long negativeCount = analysisRepository.countBySentiment("negative");
        
        // Add reaction-based sentiment (likes = positive, dislikes = negative)
        long likesCount = reactionService.countReactionsByType("LIKE");
        long dislikesCount = reactionService.countReactionsByType("DISLIKE");
        
        AnalysisStats.SentimentBreakdown sentimentBreakdown = new AnalysisStats.SentimentBreakdown();
        sentimentBreakdown.setPositive((int) (positiveCount + likesCount));
        sentimentBreakdown.setNeutral((int) neutralCount);
        sentimentBreakdown.setNegative((int) (negativeCount + dislikesCount));
        
        // Get recent alerts (last 7 days)
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<AnalysisAlert> recentAlerts = alertRepository.findByCreatedAtAfterOrderByCreatedAtDesc(weekAgo);
        
        // Get trend data for last 30 days (includes both comment analysis and reactions)
        List<AnalysisStats.TrendData> trendData = getTrendDataForDays(30);
        
        AnalysisStats stats = new AnalysisStats();
        stats.setTotalAnalyzed((int) (totalAnalyzed + likesCount + dislikesCount));
        stats.setFlaggedComments((int) flaggedComments);
        stats.setSentimentBreakdown(sentimentBreakdown);
        stats.setRecentAlerts(recentAlerts);
        stats.setTrendData(trendData);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/comments/flagged")
    public ResponseEntity<ApiResponse<PageResponse<CommentAnalysis>>> getFlaggedComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReactionService.ReactionCategory entityType,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("analyzedAt").descending());
        Page<CommentAnalysis> result;
        
        if (entityType != null || sentiment != null) {
            result = analysisRepository.findByModerationFlaggedAndFilters(true, entityType, sentiment, pageable);
        } else {
            result = analysisRepository.findByModerationFlagged(true, pageable);
        }
        
        PageResponse<CommentAnalysis> response = new PageResponse<>(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentAnalysis>>> getAnalysisByEntity(
            @RequestParam ReactionService.ReactionCategory entityType,
            @RequestParam String entityId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("analyzedAt").descending());
        Page<CommentAnalysis> result = analysisRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        
        PageResponse<CommentAnalysis> response = new PageResponse<>(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentAnalysis>> getAnalysisById(@PathVariable String id) {
        CommentAnalysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Analysis not found"));
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<PageResponse<AnalysisAlert>>> getAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<AnalysisAlert> result = alertRepository.findAll(pageable);
        
        PageResponse<AnalysisAlert> response = new PageResponse<>(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/alerts/entity")
    public ResponseEntity<ApiResponse<PageResponse<AnalysisAlert>>> getAlertsByEntity(
            @RequestParam ReactionService.ReactionCategory entityType,
            @RequestParam String entityId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<AnalysisAlert> result = alertRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        
        PageResponse<AnalysisAlert> response = new PageResponse<>(
                result.getContent(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<List<AnalysisStats.TrendData>>> getTrendData(
            @RequestParam(required = false) ReactionService.ReactionCategory entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(defaultValue = "30") int days
    ) {
        List<AnalysisStats.TrendData> trendData;
        
        if (entityType != null && entityId != null) {
            trendData = getTrendDataForEntity(entityType, entityId, days);
        } else {
            trendData = getTrendDataForDays(days);
        }
        
        return ResponseEntity.ok(ApiResponse.success(trendData));
    }

    @GetMapping("/entity-title")
    public ResponseEntity<ApiResponse<String>> getEntityTitle(
            @RequestParam ReactionService.ReactionCategory entityType,
            @RequestParam String entityId
    ) {
        // This would need to be implemented based on your entity services
        String title = "Entity " + entityType + " - " + entityId;
        return ResponseEntity.ok(ApiResponse.success(title));
    }

    @GetMapping("/comment-details/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommentDetails(
            @PathVariable String commentId,
            @RequestParam ReactionService.ReactionCategory entityType
    ) {
        Map<String, Object> details = reactionService.getCommentDetails(commentId, entityType);
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    private List<AnalysisStats.TrendData> getTrendDataForDays(int days) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);
        
        List<CommentAnalysis> analyses = analysisRepository.findByAnalyzedAtBetween(startDate, endDate);
        
        // Group by date and calculate sentiment counts
        Map<String, Map<String, Integer>> dailyData = new LinkedHashMap<>();
        
        for (int i = days - 1; i >= 0; i--) {
            Instant date = endDate.minus(i, ChronoUnit.DAYS);
            String dateKey = date.toString().substring(0, 10); // YYYY-MM-DD
            dailyData.put(dateKey, new HashMap<>());
            dailyData.get(dateKey).put("positive", 0);
            dailyData.get(dateKey).put("neutral", 0);
            dailyData.get(dateKey).put("negative", 0);
        }
        
        // Process comment analyses
        for (CommentAnalysis analysis : analyses) {
            String dateKey = analysis.getAnalyzedAt().toString().substring(0, 10);
            if (dailyData.containsKey(dateKey)) {
                String sentiment = analysis.getSentiment().toLowerCase();
                dailyData.get(dateKey).put(sentiment, 
                    dailyData.get(dateKey).getOrDefault(sentiment, 0) + 1);
            }
        }
        
        // Add reaction-based sentiment (likes = positive, dislikes = negative)
        addReactionSentimentToTrendData(dailyData, startDate, endDate);
        
        return dailyData.entrySet().stream()
                .map(entry -> {
                    AnalysisStats.TrendData trend = new AnalysisStats.TrendData();
                    trend.setDate(entry.getKey());
                    trend.setPositive(entry.getValue().get("positive"));
                    trend.setNeutral(entry.getValue().get("neutral"));
                    trend.setNegative(entry.getValue().get("negative"));
                    trend.setTotal(trend.getPositive() + trend.getNeutral() + trend.getNegative());
                    return trend;
                })
                .collect(Collectors.toList());
    }

    private List<AnalysisStats.TrendData> getTrendDataForEntity(
            ReactionService.ReactionCategory entityType, 
            String entityId, 
            int days
    ) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(days, ChronoUnit.DAYS);
        
        List<CommentAnalysis> analyses = analysisRepository.findByEntityTypeAndEntityIdAndAnalyzedAtBetween(
                entityType, entityId, startDate, endDate);
        
        // Similar grouping logic as above but filtered by entity
        Map<String, Map<String, Integer>> dailyData = new LinkedHashMap<>();
        
        for (int i = days - 1; i >= 0; i--) {
            Instant date = endDate.minus(i, ChronoUnit.DAYS);
            String dateKey = date.toString().substring(0, 10);
            dailyData.put(dateKey, new HashMap<>());
            dailyData.get(dateKey).put("positive", 0);
            dailyData.get(dateKey).put("neutral", 0);
            dailyData.get(dateKey).put("negative", 0);
        }
        
        // Process comment analyses
        for (CommentAnalysis analysis : analyses) {
            String dateKey = analysis.getAnalyzedAt().toString().substring(0, 10);
            if (dailyData.containsKey(dateKey)) {
                String sentiment = analysis.getSentiment().toLowerCase();
                dailyData.get(dateKey).put(sentiment, 
                    dailyData.get(dateKey).getOrDefault(sentiment, 0) + 1);
            }
        }
        
        // Add reaction-based sentiment for specific entity
        addEntityReactionSentimentToTrendData(dailyData, entityType, entityId, startDate, endDate);
        
        return dailyData.entrySet().stream()
                .map(entry -> {
                    AnalysisStats.TrendData trend = new AnalysisStats.TrendData();
                    trend.setDate(entry.getKey());
                    trend.setPositive(entry.getValue().get("positive"));
                    trend.setNeutral(entry.getValue().get("neutral"));
                    trend.setNegative(entry.getValue().get("negative"));
                    trend.setTotal(trend.getPositive() + trend.getNeutral() + trend.getNegative());
                    return trend;
                })
                .collect(Collectors.toList());
    }

    /**
     * Add reaction-based sentiment data to trend analysis
     * Likes count as positive, dislikes as negative
     */
    private void addReactionSentimentToTrendData(
            Map<String, Map<String, Integer>> dailyData, 
            Instant startDate, 
            Instant endDate
    ) {
        // Get likes and dislikes for the date range across all entities
        for (ReactionService.ReactionCategory category : ReactionService.ReactionCategory.values()) {
            addEntityReactionSentimentToTrendData(dailyData, category, null, startDate, endDate);
        }
    }

    /**
     * Add entity-specific reaction sentiment data
     */
    private void addEntityReactionSentimentToTrendData(
            Map<String, Map<String, Integer>> dailyData,
            ReactionService.ReactionCategory entityType,
            String entityId,
            Instant startDate,
            Instant endDate
    ) {
        // Get likes for the date range
        List<Map<String, Object>> likes = reactionService.getReactionsByTypeAndDateRange(
            "LIKE", entityType, entityId, startDate, endDate);
        
        // Get dislikes for the date range  
        List<Map<String, Object>> dislikes = reactionService.getReactionsByTypeAndDateRange(
            "DISLIKE", entityType, entityId, startDate, endDate);
            
        // Process likes (positive sentiment)
        for (Map<String, Object> like : likes) {
            Instant createdAt = (Instant) like.get("createdAt");
            if (createdAt != null) {
                String dateKey = createdAt.toString().substring(0, 10);
                if (dailyData.containsKey(dateKey)) {
                    dailyData.get(dateKey).put("positive", 
                        dailyData.get(dateKey).get("positive") + 1);
                }
            }
        }
        
        // Process dislikes (negative sentiment)
        for (Map<String, Object> dislike : dislikes) {
            Instant createdAt = (Instant) dislike.get("createdAt");
            if (createdAt != null) {
                String dateKey = createdAt.toString().substring(0, 10);
                if (dailyData.containsKey(dateKey)) {
                    dailyData.get(dateKey).put("negative", 
                        dailyData.get(dateKey).get("negative") + 1);
                }
            }
        }
    }
}
