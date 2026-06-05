package com.aicontentstudio.ai.service;

import com.aicontentstudio.ai.dto.BlogGenerationRequest;
import com.aicontentstudio.ai.dto.BlogGenerationResponse;

public interface AiBlogService {
    BlogGenerationResponse generateBlog(BlogGenerationRequest request);
}