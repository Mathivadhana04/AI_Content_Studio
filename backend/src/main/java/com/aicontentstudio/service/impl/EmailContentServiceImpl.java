package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.EmailContentRequest;
import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.EmailCampaign;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.EmailCampaignRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.service.AiContentService;
import com.aicontentstudio.service.EmailContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailContentServiceImpl implements EmailContentService {

    private final EmailCampaignRepository emailCampaignRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final AiContentService aiContentService;

    @Value("${app.rate-limit.ai-requests-per-day}")
    private int aiRequestsPerDay;

    @Override
    public EmailCampaign generateEmail(EmailContentRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        Blog blog = null;
        if (request.getSourceBlogId() != null) {
            blog = blogRepository.findById(request.getSourceBlogId())
                    .orElseThrow(() -> new ResourceNotFoundException("Blog", request.getSourceBlogId()));
            checkBlogAccess(blog, user);
        }

        String context = request.getContext() != null ? request.getContext() : "";
        if (blog != null && context.isBlank()) {
            context = "This email should be derived from the blog post titled \"" + blog.getTitle() + "\" with the following content:\n\n" + blog.getContent();
        }

        log.info("Generating email campaign type: {}, subject: {}", request.getEmailType(), request.getSubject());
        String generatedHtml = "";
        try {
            generatedHtml = aiContentService.generateEmail(
                    request.getEmailType().name(),
                    request.getSubject(),
                    context,
                    request.getTargetAudience() != null ? request.getTargetAudience() : "general audience"
            );
        } catch (Exception e) {
            log.error("AI email campaign generation failed: {}", e.getMessage());
            throw e;
        }

        // Increment user's AI requests
        user.setAiRequestsToday(user.getAiRequestsToday() + 1);
        userRepository.save(user);

        EmailCampaign campaign = EmailCampaign.builder()
                .author(user)
                .sourceBlog(blog)
                .emailType(request.getEmailType())
                .subject(request.getSubject())
                .htmlContent(generatedHtml)
                .plainTextContent(stripHtmlTags(generatedHtml))
                .targetAudience(request.getTargetAudience())
                .build();

        return emailCampaignRepository.save(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmailCampaign> getUserCampaigns(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return emailCampaignRepository.findByAuthor(user, pageable);
    }

    @Override
    public void deleteCampaign(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        EmailCampaign campaign = emailCampaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmailCampaign", id));

        if (!campaign.getAuthor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to delete this email campaign");
        }

        emailCampaignRepository.delete(campaign);
        log.info("Email campaign {} deleted by {}", id, userEmail);
    }

    // ===== Helpers =====
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void checkBlogAccess(Blog blog, User user) {
        boolean isOwner = blog.getWorkspace().getOwner().getId().equals(user.getId());
        boolean isMember = blog.getWorkspace().getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (!isOwner && !isMember) {
            throw new UnauthorizedException("You do not have access to this blog");
        }
    }

    private void checkAiRateLimit(User user) {
        if (user.getAiRequestsToday() >= aiRequestsPerDay) {
            throw new com.aicontentstudio.exception.RateLimitExceededException(aiRequestsPerDay);
        }
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }
}
