package com.cms.service;

import com.cms.client.AiClient;
import com.cms.dto.request.CommentRequest;
import com.cms.model.CommentAnalysis;
import com.cms.model.ReactionBaseDocument;
import com.cms.repository.AnalysisJobRepository;
import com.cms.repository.CommentAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
// import org.springframework.data.mongodb.core.query.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAnalysisServiceTest {

    @Mock private AiClient aiClient;
    @Mock private CommentAnalysisRepository commentAnalysisRepository;
    @Mock private AnalysisJobRepository analysisJobRepository;
    @Mock private TrendService trendService;
    @Mock private ReactionService reactionService;
    @Mock private PostService postService;
    @Mock private PublicationService publicationService;
    @Mock private EventService eventService;
    @Mock private QuoteService quoteService;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks private AiAnalysisService service;

    @Test
    void processChunkAsync_savesAnalysesFor120CommentsWithExpectedSentiments() throws Exception {
        // Arrange: 120 comments -> 3 chunks of 40
        List<CommentRequest> all = new ArrayList<>();
        // 40 positive
        all.addAll(generateComments(0, 40, "I love this product", Instant.now()));
        // 40 negative
        all.addAll(generateComments(40, 80, "This is very bad", Instant.now()));
        // 40 neutral
        all.addAll(generateComments(80, 120, "It is okay", Instant.now()));

        List<List<CommentRequest>> chunks = List.of(
                all.subList(0, 40),
                all.subList(40, 80),
                all.subList(80, 120)
        );

        when(aiClient.getModelName()).thenReturn("test-model");
        // Mock AI to return JSON array matching inputs, sentiment driven by content keywords
        when(aiClient.analyzeChunk(anyList(), anyString())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<CommentRequest> inputs = (List<CommentRequest>) invocation.getArgument(0);
            String json = inputs.stream().map(c -> {
                String content = c.content() == null ? "" : c.content().toLowerCase();
                String sentiment = content.contains("love") ? "positive" : content.contains("bad") ? "negative" : "neutral";
                double score = sentiment.equals("positive") ? 0.9 : sentiment.equals("negative") ? 0.1 : 0.5;
                return String.format("{\"commentId\":\"%s\",\"sentiment\":\"%s\",\"sentimentScore\":%s,\"moderation\":{\"flagged\":%s}}",
                        c.id(), sentiment, score, sentiment.equals("negative"));
            }).collect(Collectors.joining(","));
            return "[" + json + "]";
        });

        // No-op external effects
        doNothing().when(trendService).recomputeAggregatesAndMaybeAlert(any(), anyString());
        doNothing().when(reactionService).updateAnalysedByTargetIdAndType(anyString(), any(ReactionBaseDocument.ReactionType.class), anyBoolean(), any());
        // Return a minimal job document from findAndModify so finalizeChunk continues safely
        // when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(com.cms.model.AnalysisJob.class)))
                // .thenAnswer(inv -> new com.cms.model.AnalysisJob());

        ArgumentCaptor<List<CommentAnalysis>> captor = ArgumentCaptor.forClass(List.class);

        // Act: invoke three chunk processes
        String jobId = "job-1";
        String entityId = "entity-1";
        for (List<CommentRequest> chunk : chunks) {
            service.processChunkAsync(jobId, ReactionService.ReactionCategory.POST, entityId, chunk, "ctx");
        }

        // Assert: 3 saves, total 120 analyses with sentiment distribution 40/40/40
        verify(commentAnalysisRepository, times(3)).saveAll(captor.capture());
        List<CommentAnalysis> allSaved = captor.getAllValues().stream().flatMap(List::stream).collect(Collectors.toList());
        assertNotNull(allSaved);
        assertEquals(120, allSaved.size());

        long positives = allSaved.stream().filter(a -> "positive".equalsIgnoreCase(a.getSentiment())).count();
        long negatives = allSaved.stream().filter(a -> "negative".equalsIgnoreCase(a.getSentiment())).count();
        long neutrals = allSaved.stream().filter(a -> "neutral".equalsIgnoreCase(a.getSentiment())).count();

        assertEquals(40, positives);
        assertEquals(40, negatives);
        assertEquals(40, neutrals);

        // Also ensure reaction flags and trend recompute were invoked
        verify(reactionService, times(120)).updateAnalysedByTargetIdAndType(anyString(), eq(ReactionBaseDocument.ReactionType.COMMENT), eq(true), eq(ReactionService.ReactionCategory.POST));
        verify(trendService, times(3)).recomputeAggregatesAndMaybeAlert(eq(ReactionService.ReactionCategory.POST), eq(entityId));
        verify(aiClient, times(3)).analyzeChunk(anyList(), anyString());
    }

    private static List<CommentRequest> generateComments(int startInclusive, int endExclusive, String content, Instant baseTime) {
        return IntStream.range(startInclusive, endExclusive)
                .mapToObj(i -> new CommentRequest("c-" + i, content + " #" + i, baseTime))
                .collect(Collectors.toList());
    }
}