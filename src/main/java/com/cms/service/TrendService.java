package com.cms.service;

import com.cms.model.AnalysisAlert;
import com.cms.repository.AnalysisAlertRepository;
import com.cms.repository.CommentAnalysisRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.cms.model.CommentAnalysis;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class TrendService {

    private final CommentAnalysisRepository analysisRepo;
    private final AnalysisAlertRepository alertRepo;

    /**
     * Recompute short window aggregates and create an alert if a negative spike is detected.
     * Simple algorithm: compare negative fraction in last 7 days vs previous 7 days.
     */
    public void recomputeAggregatesAndMaybeAlert(ReactionService.ReactionCategory entityType, String entityId) {
        Instant now = Instant.now();
        Instant windowEnd = now;
        Instant windowStart = now.minus(7, ChronoUnit.DAYS);
        Instant baselineStart = now.minus(14, ChronoUnit.DAYS);
        Instant baselineEnd = now.minus(7, ChronoUnit.DAYS);

        List<CommentAnalysis> window = analysisRepo.findByEntityTypeAndEntityIdAndAnalyzedAtBetween(entityType, entityId, windowStart, windowEnd);
        List<CommentAnalysis> baseline = analysisRepo.findByEntityTypeAndEntityIdAndAnalyzedAtBetween(entityType, entityId, baselineStart, baselineEnd);

        if (window.isEmpty() || baseline.isEmpty()) return;

        long windowNegative = window.stream().filter(c -> "negative".equalsIgnoreCase(c.getSentiment())).count();
        long baselineNegative = baseline.stream().filter(c -> "negative".equalsIgnoreCase(c.getSentiment())).count();

        double windowFrac = (double) windowNegative / Math.max(1, window.size());
        double baselineFrac = (double) baselineNegative / Math.max(1, baseline.size());

        // simple rule: spike if absolute increase > 0.15 or windowFrac/baselineFrac > 1.5
        boolean spike = (windowFrac - baselineFrac) > 0.15 || (baselineFrac > 0 && (windowFrac / baselineFrac) > 1.5);

        if (spike) {
            AnalysisAlert alert = AnalysisAlert.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .metric("negative_fraction")
                    .currentValue(windowFrac)
                    .baselineValue(baselineFrac)
                    .windowStart(windowStart)
                    .windowEnd(windowEnd)
                    .severity(windowFrac - baselineFrac > 0.30 ? "CRITICAL" : "WARNING")
                    .note("Detected increase in negative comments in last 7 days")
                    .createdAt(Instant.now())
                    .build();
            alertRepo.save(alert);
        }
    }
}
