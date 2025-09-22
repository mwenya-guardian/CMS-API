package com.cms.client;

import com.cms.dto.request.CommentRequest;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GeminiChatService implements AiClient {

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
        promptBuilder.append("Analyze the following comments for sentiment, tone, and moderation flags. ");
        promptBuilder.append("Return a JSON array where each object has: id, sentiment (positive/neutral/negative), ");
        promptBuilder.append("sentimentScore (0.0-1.0), tone, toneConfidence (0.0-1.0), and moderation (flagged boolean, categories array, confidence 0.0-1.0).\n\n");
        promptBuilder.append("Context: ").append(promptContext).append("\n\n");
        promptBuilder.append("Comments to analyze:\n");

        for (CommentRequest comment : comments) {
            promptBuilder.append("ID: ").append(comment.id())
                    .append(", Content: ").append(comment.content())
                    .append(", Created: ").append(comment.createdAt())
                    .append("\n");
        }

        promptBuilder.append("\nReturn only the JSON array, no additional text.");

        // Call the Gemini model
        org.springframework.ai.chat.prompt.Prompt prompt = new org.springframework.ai.chat.prompt.Prompt(promptBuilder.toString());
        org.springframework.ai.chat.model.ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getText();
//    return " ";
    }
}