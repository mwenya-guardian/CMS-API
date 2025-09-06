package com.cms.utils;

import com.cms.dto.request.ReactionRequest;
import com.cms.dto.response.ReactionResponse;
import com.cms.model.*;
import com.cms.service.ReactionService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ReactionMappers {

    // -------------------------
    // Mapping helpers
    // -------------------------

    /**
     * Map incoming request to concrete Reaction subclass based on category.
     *
     * Note: this uses id-only constructors / setters for referenced target entities.
     * Adjust if your domain objects require other constructors or fields.
     */
    public ReactionBaseDocument mapToSpecificReaction(ReactionRequest req, ReactionService.ReactionCategory category) {
//        if (!StringUtils.hasText(req.getUserId())) {
//            throw new IllegalArgumentException("userId is required");
//        }
        if (req.getType() == null) {
            throw new IllegalArgumentException("type is required");
        }

        // create and set common fields
        ReactionBaseDocument base;
        switch (category) {
            case POST:
                PostReaction pr = new PostReaction();
                if (StringUtils.hasText(req.getTargetId())) {
                    Post p = new Post();
                    p.setId(req.getTargetId());
                    pr.setPost(p);
                }
                base = pr;
                break;
            case PUBLICATION:
                PublicationReaction pub = new PublicationReaction();
                if (StringUtils.hasText(req.getTargetId())) {
                    Publication pPub = new Publication();
                    pPub.setId(req.getTargetId());
                    pub.setPublication(pPub);
                }
                base = pub;
                break;
            case EVENT:
                EventReaction er = new EventReaction();
                if (StringUtils.hasText(req.getTargetId())) {
                    Event e = new Event();
                    e.setId(req.getTargetId());
                    er.setEvent(e);
                }
                base = er;
                break;
            case QUOTE:
                QuoteReaction qr = new QuoteReaction();
                if (StringUtils.hasText(req.getTargetId())) {
                    Quote q = new Quote();
                    q.setId(req.getTargetId());
                    qr.setQuote(q);
                }
                base = qr;
                break;
            default:
                throw new IllegalArgumentException("Unsupported category: " + category);
        }

        // common fields: user, type, comment
        User u = new User();
        base.setUser(u);
        base.setType(req.getType());
        if (req.getComment() != null) base.setComment(req.getComment());

        return base;
    }

    /**
     * Map persisted ReactionBaseDocument to a serializable ReactionResponse DTO.
     */
    public ReactionResponse mapToResponse(ReactionBaseDocument doc, ReactionService.ReactionCategory category) {
        ReactionResponse r = new ReactionResponse();
        r.setId(doc.getId());
        r.setType(doc.getType());
        r.setComment(doc.getComment());
        if (doc.getUser() != null) r.setUserId(doc.getUser().getId());
        // target id depending on category:
        switch (category) {
            case POST:
                PostReaction pr = (PostReaction) doc;
                if (pr.getPost() != null) r.setTargetId(pr.getPost().getId());
                break;
            case PUBLICATION:
                PublicationReaction pub = (PublicationReaction) doc;
                if (pub.getPublication() != null) r.setTargetId(pub.getPublication().getId());
                break;
            case EVENT:
                EventReaction er = (EventReaction) doc;
                if (er.getEvent() != null) r.setTargetId(er.getEvent().getId());
                break;
            case QUOTE:
                QuoteReaction qr = (QuoteReaction) doc;
                if (qr.getQuote() != null) r.setTargetId(qr.getQuote().getId());
                break;
            default:
                break;
        }
        r.setCreatedAt(doc.getCreatedAt()); // if you persist createdAt in BaseDocument, replace with doc.getCreatedAt()
        r.setUpdatedAt(doc.getUpdatedAt());
        return r;
    }

}
