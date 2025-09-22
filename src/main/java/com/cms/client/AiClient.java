package com.cms.client;

import com.cms.dto.request.CommentRequest;

import java.util.List;

public interface AiClient {
    /**
     * Send a chunk of comments (already prepared) to the configured model and return raw JSON response.
     * Implementation is responsible for HTTP, authentication, timeout handling.
     */
    String analyzeChunk(List<CommentRequest> comments, String promptContext) throws Exception;
    }