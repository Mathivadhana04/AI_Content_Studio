package com.aicontentstudio.service;

import com.aicontentstudio.dto.response.SeoReportResponse;

/**
 * Algorithmic SEO analysis — no AI calls, pure rule-based scoring.
 */
public interface SeoService {

    /**
     * Analyse the blog identified by blogId and persist an SeoReport.
     * Also updates blog.seoScore with the computed overallScore.
     */
    SeoReportResponse analyzeBlog(Long blogId, String userEmail, String targetKeyword);

    /**
     * Retrieve the most-recent SeoReport for the given blog.
     */
    SeoReportResponse getLatestReport(Long blogId, String userEmail);

    /**
     * Analyze arbitrary text for keyword density and readability without persisting anything to DB.
     */
    SeoReportResponse analyzeText(String text, String targetKeyword);
}
