package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.EmailContentRequest;
import com.aicontentstudio.entity.EmailCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * AI-powered email campaign generation and management.
 */
public interface EmailContentService {

    /**
     * Generate an email campaign via the AI service and persist it.
     */
    EmailCampaign generateEmail(EmailContentRequest request, String userEmail);

    /**
     * Paginated list of all email campaigns created by the given user.
     */
    Page<EmailCampaign> getUserCampaigns(String userEmail, Pageable pageable);

    /**
     * Delete a campaign owned by the given user.
     */
    void deleteCampaign(Long id, String userEmail);
}
