package com.cms.service;

import com.cms.model.*;
import com.cms.repository.EventReactionRepository;
import com.cms.repository.PostReactionRepository;
import com.cms.repository.PublicationReactionRepository;
import com.cms.repository.QuoteReactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.List;

@Service
@AllArgsConstructor
public class ReactionService {

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

        ReactionCategory category = detectCategoryFromInstance(reaction);
        if(category != reactionCategory){
            throw new IllegalArgumentException("Path category and reaction category do not match");
        }
        reaction.getUser().setId(authService.getCurrentUser().getId());
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
    // -----------------------
    // Read
    // -----------------------
    public List<? extends ReactionBaseDocument> findByTypeAndId(ReactionBaseDocument.ReactionType type, ReactionCategory category, String id) {
        Assert.notNull(type, "type must not be null");
        Assert.notNull(category, "category must not be null");

        return switch (category) {
            case POST -> postReactionRepo.findByPostIdAndType(id, type);
            case PUBLICATION -> publicationReactionRepo.findByPublicationIdAndType(id, type);
            case EVENT -> eventReactionRepo.findByEventIdAndType(id, type);
            case QUOTE -> quoteReactionRepo.findByQuoteIdAndType(id, type);
        };
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
                PostReaction existingPost = postReactionRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("PostReaction not found: " + id));
                if (!(updated instanceof PostReaction toSavePost)) {
                    throw new IllegalArgumentException("Expected PostReaction for category POST");
                }
                // preserve id and update fields (simple strategy: copy allowed fields)
                toSavePost.setId(existingPost.getId());
                return postReactionRepo.save(toSavePost);

            case PUBLICATION:
                PublicationReaction existingPub = publicationReactionRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("PublicationReaction not found: " + id));
                if (!(updated instanceof PublicationReaction toSavePub)) {
                    throw new IllegalArgumentException("Expected PublicationReaction for category PUBLICATION");
                }
                toSavePub.setId(existingPub.getId());
                return publicationReactionRepo.save(toSavePub);

            case EVENT:
                EventReaction existingEvent = eventReactionRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("EventReaction not found: " + id));
                if (!(updated instanceof EventReaction toSaveEvent)) {
                    throw new IllegalArgumentException("Expected EventReaction for category EVENT");
                }
                toSaveEvent.setId(existingEvent.getId());
                return eventReactionRepo.save(toSaveEvent);

            case QUOTE:
                QuoteReaction existingQuote = quoteReactionRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("QuoteReaction not found: " + id));
                if (!(updated instanceof QuoteReaction toSaveQuote)) {
                    throw new IllegalArgumentException("Expected QuoteReaction for category QUOTE");
                }
                toSaveQuote.setId(existingQuote.getId());
                return quoteReactionRepo.save(toSaveQuote);

            default:
                throw new IllegalArgumentException("Unsupported reaction category: " + category);
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
}
