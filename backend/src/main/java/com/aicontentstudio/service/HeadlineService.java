package com.aicontentstudio.service;

import com.aicontentstudio.entity.HeadlineVariant;

import java.util.List;

/**
 * AI-powered headline generation and selection.
 */
public interface HeadlineService {

    /**
     * Call AiContentService.generateHeadlines(), parse the JSON response and
     * persist HeadlineVariant rows (seo, professional, clickbait, linkedin).
     */
    List<HeadlineVariant> generateHeadlines(Long blogId, String userEmail);

    /**
     * Return all headline variants for the given blog.
     */
    List<HeadlineVariant> getHeadlines(Long blogId, String userEmail);

    /**
     * Mark the chosen variant as selected (clears previous selection).
     * Returns the updated blog title.
     */
    String selectHeadline(Long variantId, String userEmail);
}
