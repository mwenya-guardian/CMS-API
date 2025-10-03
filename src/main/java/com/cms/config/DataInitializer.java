package com.cms.config;

import com.cms.model.Event;
import com.cms.model.Post;
import com.cms.model.Publication;
import com.cms.model.Quote;
import com.cms.model.ReactionTrackedModel;
import com.cms.service.AuthService;
import com.cms.service.EventService;
import com.cms.service.PostService;
import com.cms.service.PublicationService;
import com.cms.service.QuoteService;
import com.cms.service.ReactionTrackedModelService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private AuthService authService;
    @SuppressWarnings("unused") // Used in populateExistingModelsForTracking method
    private PostService postService;
    @SuppressWarnings("unused") // Used in populateExistingModelsForTracking method
    private EventService eventService;
    @SuppressWarnings("unused") // Used in populateExistingModelsForTracking method
    private QuoteService quoteService;
    @SuppressWarnings("unused") // Used in populateExistingModelsForTracking method
    private PublicationService publicationService;
    @SuppressWarnings("unused") // Used in populateExistingModelsForTracking method
    private ReactionTrackedModelService reactionTrackedModelService;
    // private NewsletterSubscriberService newsletterSubscriberService;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user
        authService.createDefaultAdmin();
        
        // TEMPORARY: Populate ReactionTrackedModel collection with existing models
        populateExistingModelsForTracking();
        
        // NewsletterSubscriberResponse news = newsletterSubscriberService.subscribe("mwenyagenesismg@gmail.com");
        // List<NewsletterSubscriberResponse> list = newsletterSubscriberService.getAll();
        System.out.println("Default admin user created: admin@cms.com / admin123");
        // System.out.println(list.isEmpty());
    }
    
    /**
     * TEMPORARY METHOD: Populate ReactionTrackedModel collection with existing models
     * This method should be removed after initial migration is complete
     */
    private void populateExistingModelsForTracking() {
        try {
            System.out.println("Starting migration of existing models to ReactionTrackedModel collection...");
            
            // Track existing Posts
            List<Post> existingPosts = postService.getPageResponse(1, Integer.MAX_VALUE).getData();
            for (Post post : existingPosts) {
                if (!reactionTrackedModelService.isModelTracked(post.getId(), ReactionTrackedModel.ModelType.POST)) {
                    reactionTrackedModelService.trackModel(post.getId(), ReactionTrackedModel.ModelType.POST);
                    System.out.println("Tracked existing Post: " + post.getId());
                }
            }
            
            // Track existing Events
            List<Event> existingEvents = eventService.getAllEvents(null, null, null, null, null, null);
            for (Event event : existingEvents) {
                if (!reactionTrackedModelService.isModelTracked(event.getId(), ReactionTrackedModel.ModelType.EVENT)) {
                    reactionTrackedModelService.trackModel(event.getId(), ReactionTrackedModel.ModelType.EVENT);
                    System.out.println("Tracked existing Event: " + event.getId());
                }
            }
            
            // Track existing Quotes
            List<Quote> existingQuotes = quoteService.getAllQuotes(null, null, null, null, null, null);
            for (Quote quote : existingQuotes) {
                if (!reactionTrackedModelService.isModelTracked(quote.getId(), ReactionTrackedModel.ModelType.QUOTE)) {
                    reactionTrackedModelService.trackModel(quote.getId(), ReactionTrackedModel.ModelType.QUOTE);
                    System.out.println("Tracked existing Quote: " + quote.getId());
                }
            }
            
            // Track existing Publications
            List<Publication> existingPublications = publicationService.getAllPublications(null, null, null, null, null, null);
            for (Publication publication : existingPublications) {
                if (!reactionTrackedModelService.isModelTracked(publication.getId(), ReactionTrackedModel.ModelType.PUBLICATION)) {
                    reactionTrackedModelService.trackModel(publication.getId(), ReactionTrackedModel.ModelType.PUBLICATION);
                    System.out.println("Tracked existing Publication: " + publication.getId());
                }
            }
            
            System.out.println("Migration completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}