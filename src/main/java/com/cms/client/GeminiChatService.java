package com.cms.client;

import com.cms.dto.request.CommentRequest;
import com.google.api.client.util.Value;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class GeminiChatService implements AiClient {
    @Getter
//    @Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
    private final String modelName = "genimi-2.0-flash";

    private final VertexAiGeminiChatModel chatModel;

    public String chat(String userPrompt) {
        ChatResponse response = chatModel.call(new Prompt(userPrompt));

        return response.getResult().getOutput().getText();
    }
    @PostConstruct
    public void log() {
        System.out.println("GOOGLE_CLOUD_PROJECT=" + System.getenv("GOOGLE_CLOUD_PROJECT"));
        System.out.println("GOOGLE_APPLICATION_CREDENTIALS=" + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        System.out.println("GOOGLE_API_KEY=" + System.getenv("GOOGLE_API_KEY"));
    }

    @Override
    public String analyzeChunk(List<CommentRequest> comments, String promptContext) throws Exception {
        // Build the prompt for sentiment analysis
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Analyze the following comments for sentiment and moderation flags. ");
        promptBuilder.append("Return a JSON array where each object has: commentId, sentiment (positive/neutral/negative), ");
        promptBuilder.append("sentimentScore (0.0-1.0) and moderation (flagged boolean, categories array, confidence 0.0-1.0).\n\n");
        promptBuilder.append("Context: ").append(promptContext).append("\n\n");
        promptBuilder.append(ModerationPromptBuilder.buildPromptForComment());
        promptBuilder.append("Comments to analyze:\n");

        for (CommentRequest comment : comments) {
            promptBuilder.append("ID: ").append(comment.id())
                    .append(", Content: ").append(comment.content())
                    .append(", Created: ").append(comment.createdAt())
                    .append("\n");
        }

        promptBuilder.append("\nExample JSON format:\n");
        promptBuilder.append("[\n");
        promptBuilder.append("  {\n");
        promptBuilder.append("    \"commentId\": \"comment123\",\n");
        promptBuilder.append("    \"sentiment\": \"positive\",\n");
        promptBuilder.append("    \"sentimentScore\": 0.85,\n");
        promptBuilder.append("    \"moderation\": {\n");
        promptBuilder.append("      \"flagged\": false,\n");
        promptBuilder.append("      \"categories\": [],\n");
        promptBuilder.append("      \"confidence\": 0.1\n");
        promptBuilder.append("    }\n");
        promptBuilder.append("  },\n");
        promptBuilder.append("  {\n");
        promptBuilder.append("    \"commentId\": \"comment456\",\n");
        promptBuilder.append("    \"sentiment\": \"negative\",\n");
        promptBuilder.append("    \"sentimentScore\": 0.2,\n");
        promptBuilder.append("    \"moderation\": {\n");
        promptBuilder.append("      \"flagged\": true,\n");
        promptBuilder.append("      \"categories\": [\"Bullying\", \"Verbal Abuse\"],\n");
        promptBuilder.append("      \"confidence\": 0.9\n");
        promptBuilder.append("    }\n");
        promptBuilder.append("  }\n");
        promptBuilder.append("]\n\n");
        promptBuilder.append("Return only the JSON array, no additional text or explanations.");

        // Call the Gemini model
        Prompt prompt = new Prompt(promptBuilder.toString());
        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getText();
    }
}