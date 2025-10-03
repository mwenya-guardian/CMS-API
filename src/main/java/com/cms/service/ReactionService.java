package com.cms.service;

import com.cms.dto.request.CommentRequest;
import com.cms.dto.response.PageResponse;
import com.cms.exception.DuplicateResourceException;
import com.cms.model.*;
import com.cms.model.ReactionBaseDocument.ReactionType;
import com.cms.repository.EventReactionRepository;
import com.cms.repository.PostReactionRepository;
import com.cms.repository.PublicationReactionRepository;
import com.cms.repository.QuoteReactionRepository;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ReactionService {
    private MongoTemplate mongoTemplate;

    private final PostReactionRepository postReactionRepo;
    private final PublicationReactionRepository publicationReactionRepo;
    private final EventReactionRepository eventReactionRepo;
    private final QuoteReactionRepository quoteReactionRepo;
    private final AuthService authService;

    public enum ReactionCategory {
        POST, PUBLICATION, EVENT, QUOTE
    }

    // -----------------------
    // Create
    // -----------------------
    /**
     * Create reaction with explicit category routing.
     */
    private ReactionBaseDocument createReactionByCategory(ReactionBaseDocument reaction, ReactionCategory category) {
        Assert.notNull(reaction, "reaction must not be null");
        Assert.notNull(category, "category must not be null");
        if(reaction.getType() != ReactionBaseDocument.ReactionType.COMMENT){
            reaction.setComment(null);
        }

        return switch (category) {
            case POST -> {
                if (!(reaction instanceof PostReaction)) {
                    throw new IllegalArgumentException("Expected PostReaction for category POST");
                }
                yield postReactionRepo.save((PostReaction) reaction);
            }
            case PUBLICATION -> {
                if (!(reaction instanceof PublicationReaction)) {
                    throw new IllegalArgumentException("Expected PublicationReaction for category PUBLICATION");
                }
                yield publicationReactionRepo.save((PublicationReaction) reaction);
            }
            case EVENT -> {
                if (!(reaction instanceof EventReaction)) {
                    throw new IllegalArgumentException("Expected EventReaction for category EVENT");
                }
                yield eventReactionRepo.save((EventReaction) reaction);
            }
            case QUOTE -> {
                if (!(reaction instanceof QuoteReaction)) {
                    throw new IllegalArgumentException("Expected QuoteReaction for category QUOTE");
                }
                yield quoteReactionRepo.save((QuoteReaction) reaction);
            }
            default -> throw new IllegalArgumentException("Unsupported reaction category: " + category);
        };
    }

    /**
     * Convenience create that detects category by instance type.
     * Useful when caller already provides correct subclass.
     */
    public ReactionBaseDocument createReaction(ReactionBaseDocument reaction, ReactionCategory reactionCategory) {
        Assert.notNull(reaction, "reaction must not be null");

        String currentUserId = authService.getCurrentUser().getId();
        ReactionCategory category = detectCategoryFromInstance(reaction);
        if(category != reactionCategory){
            throw new IllegalArgumentException("Path category and reaction category do not match");
        }
        if(reaction.getType() == ReactionBaseDocument.ReactionType.LIKE
           || reaction.getType() == ReactionBaseDocument.ReactionType.DISLIKE){
            List<? extends ReactionBaseDocument> currentReaction = switch (category) {
                case POST -> postReactionRepo.findByTypeNotAndUserIdAndPostId(ReactionBaseDocument.ReactionType.COMMENT, currentUserId,((PostReaction) reaction).getPost().getId());
                case PUBLICATION -> publicationReactionRepo.findByTypeNotAndUserIdAndPublicationId(ReactionBaseDocument.ReactionType.COMMENT, currentUserId,((PublicationReaction) reaction).getPublication().getId());
                case EVENT -> eventReactionRepo.findByTypeNotAndUserIdAndEventId(ReactionBaseDocument.ReactionType.COMMENT, currentUserId,((EventReaction) reaction).getEvent().getId());
                case QUOTE -> quoteReactionRepo.findByTypeNotAndUserIdAndQuoteId(ReactionBaseDocument.ReactionType.COMMENT, currentUserId,((QuoteReaction) reaction).getQuote().getId());
            };

            if(!currentReaction.isEmpty()){
                currentReaction.removeFirst();
                currentReaction.stream().peek((react) -> {
                    if(reaction.getType() == ReactionBaseDocument.ReactionType.LIKE
                            || reaction.getType() == ReactionBaseDocument.ReactionType.DISLIKE){
                        deleteReaction(react.getId(), category);
                    }
                });
                throw new DuplicateResourceException("LIKE OR DISLIKE is already present");
            }
        }
        reaction.getUser().setId(currentUserId);
        return createReactionByCategory(reaction, category);
    }

    // -----------------------
    // Read
    // -----------------------
    public Optional<? extends ReactionBaseDocument> findById(String id, ReactionCategory category) {
        Assert.hasText(id, "id must not be empty");
        Assert.notNull(category, "category must not be null");

        return switch (category) {
            case POST -> postReactionRepo.findById(id);
            case PUBLICATION -> publicationReactionRepo.findById(id);
            case EVENT -> eventReactionRepo.findById(id);
            case QUOTE -> quoteReactionRepo.findById(id);
        };
    }
    public List<? extends ReactionBaseDocument> findByTypeAndTargetId(ReactionBaseDocument.ReactionType type, ReactionCategory category, String targetId) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(category, "category must not be null");

        return switch (category) {
            case POST -> postReactionRepo.findByPostIdAndType(targetId, type);
            case PUBLICATION -> publicationReactionRepo.findByPublicationIdAndType(targetId, type);
            case EVENT -> eventReactionRepo.findByEventIdAndType(targetId, type);
            case QUOTE -> quoteReactionRepo.findByQuoteIdAndType(targetId, type);

        };
    }
    public Page<? extends ReactionBaseDocument> findByTypeAndTargetIdPagedForAi(ReactionBaseDocument.ReactionType type, ReactionCategory category, String targetId, int limit, int page) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(category, "category must not be null");

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return switch (category) {
            case POST -> postReactionRepo.findByPostIdAndTypeAndAnalysedFalse(targetId,type, pageable);
            case PUBLICATION -> publicationReactionRepo.findByPublicationIdAndTypeAndAnalysedFalse(targetId, type, pageable);
            case EVENT -> eventReactionRepo.findByEventIdAndTypeAndAnalysedFalse(targetId, type, pageable);
            case QUOTE -> quoteReactionRepo.findByQuoteIdAndTypeAndAnalysedFalse(targetId, type, pageable);
        };
    }public Page<? extends ReactionBaseDocument> findByTypeAndTargetIdPaged(ReactionBaseDocument.ReactionType type, ReactionCategory category, String targetId, int limit, int page) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(category, "category must not be null");

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return switch (category) {
            case POST -> postReactionRepo.findByPostIdAndType(targetId,type, pageable);
            case PUBLICATION -> publicationReactionRepo.findByPublicationIdAndType(targetId, type, pageable);
            case EVENT -> eventReactionRepo.findByEventIdAndType(targetId, type, pageable);
            case QUOTE -> quoteReactionRepo.findByQuoteIdAndType(targetId, type, pageable);
        };
    }
    public List<? extends ReactionBaseDocument> findByTypeAndUserId(ReactionBaseDocument.ReactionType type, ReactionCategory category, String userId) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(category, "category must not be null");

        return switch (category) {
            case POST -> postReactionRepo.findByUserIdAndType(userId, type);
            case PUBLICATION -> publicationReactionRepo.findByUserIdAndType(userId, type);
            case EVENT -> eventReactionRepo.findByUserIdAndType(userId, type);
            case QUOTE -> quoteReactionRepo.findByUserIdAndType(userId, type);
        };
    }
    public long countByTypeAndTargetId(ReactionBaseDocument.ReactionType type, ReactionCategory category, String id) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(category, "category must not be null");

        return switch (category) {
            case POST -> postReactionRepo.countByPostIdAndType(id, type);
            case PUBLICATION -> publicationReactionRepo.countByPublicationIdAndType(id, type);
            case EVENT -> eventReactionRepo.countByEventIdAndType(id, type);
            case QUOTE -> quoteReactionRepo.countByQuoteIdAndType(id, type);
        };
    }
    public HashMap<ReactionType, Long> countByTargetId(ReactionCategory category, String id) {
        HashMap<ReactionType, Long> reactionCountMap = new HashMap<>();
        Long likeCount = countByTypeAndTargetId(ReactionType.LIKE, category, id);
        Long dislikeCount = countByTypeAndTargetId(ReactionType.DISLIKE, category, id);
        Long commentCount = countByTypeAndTargetId(ReactionType.COMMENT, category, id);
        reactionCountMap.put(ReactionType.LIKE, likeCount);
        reactionCountMap.put(ReactionType.DISLIKE, dislikeCount);
        reactionCountMap.put(ReactionType.COMMENT, commentCount);

        return reactionCountMap;
    }
    public List<? extends ReactionBaseDocument> findByUserIdAndTypeAndTargetId(String userId, ReactionBaseDocument.ReactionType type, ReactionCategory category, String targetId) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(category, "category must not be null");

        return switch (category) {
            case POST -> postReactionRepo.findByUserIdAndPostIdAndType(userId, targetId, type);
            case PUBLICATION -> publicationReactionRepo.findByUserIdAndPublicationIdAndType(userId, targetId, type);
            case EVENT -> eventReactionRepo.findByUserIdAndEventIdAndType(userId, targetId, type);
            case QUOTE -> quoteReactionRepo.findByUserIdAndQuoteIdAndType(userId, targetId, type);
        };
    }
    public PageResponse<CommentRequest> getCommentForAnalysis(String targetId, ReactionCategory category, int limit, int page){
         Page<? extends ReactionBaseDocument> comments = findByTypeAndTargetIdPagedForAi(ReactionType.COMMENT, category, targetId, limit, page);
         List<CommentRequest> commentRequests = comments.getContent()
                .stream().map(
                        (comment)->{
                            return new CommentRequest(comment.getId(), comment.getComment(), comment.getCreatedAt());
                        }
                ).toList();
         return new PageResponse<>(commentRequests, comments.getNumber() + 1, comments.getSize(), comments.getTotalElements());

    }

    // -----------------------
    // Update
    // -----------------------
    /**
     * Update reaction by id. Caller must pass a ReactionBaseDocument (subclass) with updated fields.
     * The id parameter is authoritative: it ensures we update the correct document.
     */
    public ReactionBaseDocument updateReaction(String id, ReactionBaseDocument updated, ReactionCategory category) {
        Assert.hasText(id, "id must not be empty");
        Assert.notNull(updated, "updated reaction must not be null");
        Assert.notNull(category, "category must not be null");
        if(updated.getType() != ReactionBaseDocument.ReactionType.COMMENT){
            updated.setComment(null);
        }
        
        switch (category) {
            case POST:
                PostReaction existing = postReactionRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("PostReaction not found: " + id));
                if (!(updated instanceof PostReaction toSave)) {
                    throw new IllegalArgumentException("Expected PostReaction for category POST");
                }
                if(existing.getType() != ReactionType.COMMENT && toSave.getType() != ReactionType.COMMENT){
                // preserve id and update fields (simple strategy: copy allowed fields)
                    toSave.setId(existing.getId());
                    return postReactionRepo.save(toSave);
                } else if(existing.getType() == toSave.getType()) {
                    toSave.setId(existing.getId());
                    return postReactionRepo.save(toSave);
                } else {
                    throw new RuntimeException("Reaction type mismatch");
                }

            case PUBLICATION:
                PublicationReaction existingPub = publicationReactionRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("PublicationReaction not found: " + id));
                if (!(updated instanceof PublicationReaction toSavePub)) {
                    throw new IllegalArgumentException("Expected PublicationReaction for category PUBLICATION");
                }
                if(existingPub.getType() != ReactionType.COMMENT && toSavePub.getType() != ReactionType.COMMENT){
                    // preserve id and update fields (simple strategy: copy allowed fields)
                        toSavePub.setId(existingPub.getId());
                        return publicationReactionRepo.save(toSavePub);
                } else if(existingPub.getType() == toSavePub.getType()) {
                        toSavePub.setId(existingPub.getId());
                        return publicationReactionRepo.save(toSavePub);
                } else {
                        throw new RuntimeException("Reaction type mismatch");
                }

            case EVENT:
                EventReaction existingEvent = eventReactionRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("EventReaction not found: " + id));
                if (!(updated instanceof EventReaction toSaveEvent)) {
                    throw new IllegalArgumentException("Expected EventReaction for category EVENT");
                }
                if(existingEvent.getType() != ReactionType.COMMENT && toSaveEvent.getType() != ReactionType.COMMENT){
                    // preserve id and update fields (simple strategy: copy allowed fields)
                        toSaveEvent.setId(existingEvent.getId());
                        return eventReactionRepo.save(toSaveEvent);
                    } else if(existingEvent.getType() == toSaveEvent.getType()) {
                        toSaveEvent.setId(existingEvent.getId());
                        return eventReactionRepo.save(toSaveEvent);
                    } else {
                        throw new RuntimeException("Reaction type mismatch");
                    }

            case QUOTE:
                QuoteReaction existingQuote = quoteReactionRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("QuoteReaction not found: " + id));
                if (!(updated instanceof QuoteReaction toSaveQuote)) {
                    throw new IllegalArgumentException("Expected QuoteReaction for category QUOTE");
                }
                if(existingQuote.getType() != ReactionType.COMMENT && toSaveQuote.getType() != ReactionType.COMMENT){
                    // preserve id and update fields (simple strategy: copy allowed fields)
                        toSaveQuote.setId(existingQuote.getId());
                        return quoteReactionRepo.save(toSaveQuote);
                    } else if(existingQuote.getType() == toSaveQuote.getType()) {
                        toSaveQuote.setId(existingQuote.getId());
                        return quoteReactionRepo.save(toSaveQuote);
                    } else {
                        throw new RuntimeException("Reaction type mismatch");
                    }

            default:
                throw new IllegalArgumentException("Unsupported reaction category: " + category);
        }
    }
    public void updateAnalysedByTargetIdAndType(String Id, ReactionBaseDocument.ReactionType type, Boolean analysed, ReactionCategory category) {
        Assert.notNull(Id, "Id must not be null");
        Assert.notNull(type, "type must not be null");
        Assert.notNull(category, "category must not be null");
        Assert.notNull(analysed, "analysed must not be null");

        Query query = new Query(Criteria.where("id").is(Id));
        Update update = new Update().set("analysed", analysed);
        switch (category) {
            case POST:
                mongoTemplate.updateFirst(query, update, PostReaction.class);
                return;
            case PUBLICATION:
                mongoTemplate.updateFirst(query, update, PublicationReaction.class);
                return;
            case EVENT:
                mongoTemplate.updateFirst(query, update, EventReaction.class);
                return;
            case QUOTE:
                mongoTemplate.updateFirst(query, update, QuoteReaction.class);
                return;
        }
    }

    // -----------------------
    // Delete
    // -----------------------
    public void deleteReaction(String id, ReactionCategory category) {
        Assert.hasText(id, "id must not be empty");
        Assert.notNull(category, "category must not be null");

        switch (category) {
            case POST:
                postReactionRepo.deleteById(id);
                return;
            case PUBLICATION:
                publicationReactionRepo.deleteById(id);
                return;
            case EVENT:
                eventReactionRepo.deleteById(id);
                return;
            case QUOTE:
                quoteReactionRepo.deleteById(id);
                return;
            default:
                throw new IllegalArgumentException("Unsupported reaction category: " + category);
        }
    }
    public void deleteReactionByUserAndTargetAndLikeOrDislike(ReactionCategory category, String targetId, ReactionType type) {
        if(type != ReactionType.LIKE && type != ReactionType.DISLIKE){
            throw new IllegalArgumentException("Unsupported reaction type");
        }
        Assert.notNull(category, "category must not be null");

        String userId = authService.getCurrentUser().getId();

        switch (category) {
            case POST:
                postReactionRepo.deleteByPostIdAndUserIdAndType(targetId, userId, type);;
                return;
            case PUBLICATION:
                publicationReactionRepo.deleteByPublicationIdAndUserIdAndType(targetId, userId, type);;
                return;
            case EVENT:
                eventReactionRepo.deleteByEventIdAndUserIdAndType(targetId, userId, type);;
                return;
            case QUOTE:
                quoteReactionRepo.deleteByQuoteIdAndUserIdAndType(targetId, userId, type);;
                return;
            default:
                throw new IllegalArgumentException("Unsupported reaction category: " + category);
        }
    }

    // -----------------------
    // Helpers
    // -----------------------
    /**
     * Infer category from runtime type of the reaction instance.
     * Throws IllegalArgumentException when it can't be inferred.
     */
    private ReactionCategory detectCategoryFromInstance(ReactionBaseDocument reaction) {
        if (reaction instanceof PostReaction) return ReactionCategory.POST;
        if (reaction instanceof PublicationReaction) return ReactionCategory.PUBLICATION;
        if (reaction instanceof EventReaction) return ReactionCategory.EVENT;
        if (reaction instanceof QuoteReaction) return ReactionCategory.QUOTE;
        throw new IllegalArgumentException("Unable to determine ReactionCategory from instance: " + reaction.getClass().getName());
    }

    // -----------------------
    // Analysis Support Methods
    // -----------------------
    
    /**
     * Count reactions by type across all categories
     */
    public long countReactionsByType(String reactionType) {
        ReactionType type = ReactionType.valueOf(reactionType.toUpperCase());
        
        long totalCount = 0;
        for (ReactionCategory category : ReactionCategory.values()) {
            totalCount += countReactionsByTypeAndCategory(type, category);
        }
        return totalCount;
    }
    
    /**
     * Count reactions by type for a specific category
     */
    public long countReactionsByTypeAndCategory(ReactionType type, ReactionCategory category) {
        return switch (category) {
            case POST -> postReactionRepo.countByType(type);
            case PUBLICATION -> publicationReactionRepo.countByType(type);
            case EVENT -> eventReactionRepo.countByType(type);
            case QUOTE -> quoteReactionRepo.countByType(type);
        };
    }
    
    /**
     * Get reactions by type and date range for trend analysis
     */
    public List<Map<String, Object>> getReactionsByTypeAndDateRange(
            String reactionType, 
            ReactionCategory entityType, 
            String entityId, 
            java.time.Instant startDate, 
            java.time.Instant endDate
    ) {
        ReactionType type = ReactionType.valueOf(reactionType.toUpperCase());
        
        if (entityId != null) {
            // Get reactions for specific entity
            return getReactionsByTypeEntityAndDateRange(type, entityType, entityId, startDate, endDate);
        } else {
            // Get reactions for all entities of this type
            return getReactionsByTypeAndCategoryAndDateRange(type, entityType, startDate, endDate);
        }
    }
    
    /**
     * Get reactions for a specific entity and date range
     */
    private List<Map<String, Object>> getReactionsByTypeEntityAndDateRange(
            ReactionType type, 
            ReactionCategory category, 
            String entityId, 
            java.time.Instant startDate, 
            java.time.Instant endDate
    ) {
        List<? extends ReactionBaseDocument> reactions = switch (category) {
            case POST -> postReactionRepo.findByPostIdAndTypeAndCreatedAtBetween(entityId, type, startDate, endDate);
            case PUBLICATION -> publicationReactionRepo.findByPublicationIdAndTypeAndCreatedAtBetween(entityId, type, startDate, endDate);
            case EVENT -> eventReactionRepo.findByEventIdAndTypeAndCreatedAtBetween(entityId, type, startDate, endDate);
            case QUOTE -> quoteReactionRepo.findByQuoteIdAndTypeAndCreatedAtBetween(entityId, type, startDate, endDate);
        };
        
        return reactions.stream()
                .map(reaction -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", reaction.getId());
                    map.put("createdAt", reaction.getCreatedAt());
                    map.put("type", reaction.getType());
                    return map;
                })
                .toList();
    }
    
    /**
     * Get reactions for all entities of a category and date range
     */
    private List<Map<String, Object>> getReactionsByTypeAndCategoryAndDateRange(
            ReactionType type, 
            ReactionCategory category, 
            java.time.Instant startDate, 
            java.time.Instant endDate
    ) {
        List<? extends ReactionBaseDocument> reactions = switch (category) {
            case POST -> postReactionRepo.findByTypeAndCreatedAtBetween(type, startDate, endDate);
            case PUBLICATION -> publicationReactionRepo.findByTypeAndCreatedAtBetween(type, startDate, endDate);
            case EVENT -> eventReactionRepo.findByTypeAndCreatedAtBetween(type, startDate, endDate);
            case QUOTE -> quoteReactionRepo.findByTypeAndCreatedAtBetween(type, startDate, endDate);
        };
        
        return reactions.stream()
                .map(reaction -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", reaction.getId());
                    map.put("createdAt", reaction.getCreatedAt());
                    map.put("type", reaction.getType());
                    return map;
                })
                .toList();
    }

    /**
     * Get comment details by comment ID and category
     */
    public Map<String, Object> getCommentDetails(String commentId, ReactionCategory category) {
        Optional<? extends ReactionBaseDocument> reactionOpt = findById(commentId, category);
        
        if (reactionOpt.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }
        
        ReactionBaseDocument reaction = reactionOpt.get();
        
        if (reaction.getType() != ReactionType.COMMENT) {
            throw new RuntimeException("Not a comment");
        }
        
        Map<String, Object> details = new HashMap<>();
        details.put("id", reaction.getId());
        details.put("content", reaction.getComment());
        details.put("createdAt", reaction.getCreatedAt());
        details.put("userId", reaction.getUser().getId());
        
        return details;
    }

}
