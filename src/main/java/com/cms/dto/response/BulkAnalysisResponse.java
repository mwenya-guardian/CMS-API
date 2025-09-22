package com.cms.dto.response;

public record BulkAnalysisResponse(
    String jobId,
    int submittedChunks
){};
