package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "reaction_tracked_models")
public class ReactionTrackedModel extends BaseDocument {
    
    @Id
    private String id;
    
    @NotBlank(message = "Model ID is required")
    @Indexed
    private String modelId;
    
    @NotNull(message = "Model type is required")
    @Indexed
    private ModelType modelType;
    
//    @NotNull(message = "Comments analyzed status is required")
//    private Boolean commentsAnalyzed = false;
    
    public enum ModelType {
        POST, PUBLICATION, EVENT, QUOTE
    }
    
    public ReactionTrackedModel(String modelId, ModelType modelType) {
        this.modelId = modelId;
        this.modelType = modelType;
//        this.commentsAnalyzed = false;
    }
    
    public ReactionTrackedModel(String modelId, ModelType modelType, Boolean commentsAnalyzed) {
        this.modelId = modelId;
        this.modelType = modelType;
//        this.commentsAnalyzed = commentsAnalyzed != null ? commentsAnalyzed : false;
    }
}
