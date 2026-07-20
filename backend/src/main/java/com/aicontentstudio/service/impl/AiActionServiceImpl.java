package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.AiInlineActionRequest;
import com.aicontentstudio.dto.request.AiRewriteRequest;
import com.aicontentstudio.entity.AiRequest;
import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.AiRequestRepository;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.repository.SocialPostRepository;
import com.aicontentstudio.repository.EmailCampaignRepository;
import com.aicontentstudio.service.AiActionService;
import com.aicontentstudio.service.AiContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AiActionServiceImpl implements AiActionService {

    private final AiContentService aiContentService;
    private final AiRequestRepository aiRequestRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final SocialPostRepository socialPostRepository;
    private final EmailCampaignRepository emailCampaignRepository;

    @Value("${app.rate-limit.ai-requests-per-day}")
    private int aiRequestsPerDay;

    @Override
    public String rewrite(AiRewriteRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        Blog blog = null;
        if (request.getBlogId() != null) {
            blog = blogRepository.findById(request.getBlogId())
                    .orElseThrow(() -> new ResourceNotFoundException("Blog", request.getBlogId()));
            checkBlogAccess(blog, user);
        }

        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        String result = "";

        try {
            result = aiContentService.rewriteText(request.getText(), request.getTone());
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            logAiRequest(user, "REWRITE_" + request.getTone().name(), System.currentTimeMillis() - startTime, success, errorMsg, blog);
            incrementUserAiCount(user);
        }

        return result;
    }

    @Override
    public String inlineAction(AiInlineActionRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        Blog blog = null;
        if (request.getBlogId() != null) {
            blog = blogRepository.findById(request.getBlogId())
                    .orElseThrow(() -> new ResourceNotFoundException("Blog", request.getBlogId()));
            checkBlogAccess(blog, user);
        }

        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        String result = "";

        try {
            switch (request.getAction().toLowerCase()) {
                case "expand":
                    result = aiContentService.expandText(request.getText());
                    break;
                case "shorten":
                    result = aiContentService.shortenText(request.getText());
                    break;
                case "fix_grammar":
                    result = aiContentService.fixGrammar(request.getText());
                    break;
                case "improve_seo":
                    result = aiContentService.improveSeo(request.getText(), request.getKeywords());
                    break;
                default:
                    throw new BadRequestException("Unknown AI action: " + request.getAction());
            }
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            logAiRequest(user, "INLINE_" + request.getAction().toUpperCase(), System.currentTimeMillis() - startTime, success, errorMsg, blog);
            incrementUserAiCount(user);
        }

        return result;
    }

    @Override
    public String askAboutContent(Long blogId, String question, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));
        checkBlogAccess(blog, user);

        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        String result = "";

        try {
            result = aiContentService.askAboutContent(question, blog.getContent());
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            logAiRequest(user, "Q_AND_A", System.currentTimeMillis() - startTime, success, errorMsg, blog);
            incrementUserAiCount(user);
        }

        return result;
    }

    @Override
    public com.aicontentstudio.dto.response.ChatbotResponse handleUniversalChat(
            com.aicontentstudio.dto.request.ChatbotRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        String contextType = request.getContextType() != null ? request.getContextType().toUpperCase() : "NONE";
        String content = null;
        Blog blog = null;
        com.aicontentstudio.entity.SocialPost socialPost = null;
        com.aicontentstudio.entity.EmailCampaign emailCampaign = null;

        if (request.getContextId() != null) {
            if (contextType.equals("BLOG")) {
                blog = blogRepository.findById(request.getContextId())
                        .orElseThrow(() -> new ResourceNotFoundException("Blog", request.getContextId()));
                checkBlogAccess(blog, user);
                content = blog.getContent();
            } else if (contextType.equals("SOCIAL")) {
                socialPost = socialPostRepository.findById(request.getContextId())
                        .orElseThrow(() -> new ResourceNotFoundException("SocialPost", request.getContextId()));
                if (!socialPost.getAuthor().getId().equals(user.getId())) {
                    throw new UnauthorizedException("You do not have access to this social post");
                }
                content = socialPost.getContent();
            } else if (contextType.equals("EMAIL")) {
                emailCampaign = emailCampaignRepository.findById(request.getContextId())
                        .orElseThrow(() -> new ResourceNotFoundException("EmailCampaign", request.getContextId()));
                if (!emailCampaign.getAuthor().getId().equals(user.getId())) {
                    throw new UnauthorizedException("You do not have access to this email campaign");
                }
                content = emailCampaign.getHtmlContent();
            }
        }

        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        String rawResponse = "";

        try {
            rawResponse = aiContentService.askChatbot(request.getMessage(), content, contextType);
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            logAiRequest(user, "UNIVERSAL_CHAT", System.currentTimeMillis() - startTime, success, errorMsg, blog);
            incrementUserAiCount(user);
        }

        // Parse JSON response
        String reply = "Sorry, I couldn't formulate a response.";
        String updatedContent = null;
        boolean contentUpdated = false;

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String cleaned = cleanJsonString(rawResponse);
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(cleaned);
            if (root.has("reply")) {
                reply = root.get("reply").asText();
            }
            if (root.has("updatedContent") && !root.get("updatedContent").isNull() && !root.get("updatedContent").asText().isEmpty() && !root.get("updatedContent").asText().equals("null")) {
                updatedContent = root.get("updatedContent").asText();
                contentUpdated = true;

                // Save back to DB
                if (contextType.equals("BLOG") && blog != null) {
                    blog.setContent(updatedContent);
                    blog.setWordCount(countWords(updatedContent));
                    blogRepository.save(blog);
                } else if (contextType.equals("SOCIAL") && socialPost != null) {
                    socialPost.setContent(updatedContent);
                    socialPostRepository.save(socialPost);
                } else if (contextType.equals("EMAIL") && emailCampaign != null) {
                    emailCampaign.setHtmlContent(updatedContent);
                    emailCampaign.setPlainTextContent(updatedContent.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim());
                    emailCampaignRepository.save(emailCampaign);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse chatbot response JSON. Raw: {}", rawResponse, e);
            reply = rawResponse;
        }

        return com.aicontentstudio.dto.response.ChatbotResponse.builder()
                .reply(reply)
                .updatedContent(updatedContent)
                .contentUpdated(contentUpdated)
                .build();
    }

    @Override
    public String generateAdCopy(com.aicontentstudio.dto.request.AdCopyRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        String prompt = String.format("""
                You are a world-class marketing copywriter. Generate 3 structured, platform-optimized ad copy variations for platform: %s.
                Product Name: %s
                Product Description: %s
                Target Audience: %s.

                FORMAT REQUIREMENTS:
                Structure each variation clearly using Markdown headings and bold key sections:

                ### 🚀 Variation 1: [Catchy Theme Name]
                - **Headline / Hook**: ...
                - **Primary Text**: ...
                - **Call to Action (CTA)**: ...

                ### 💡 Variation 2: [Benefit-Driven Theme]
                - **Headline / Hook**: ...
                - **Primary Text**: ...
                - **Call to Action (CTA)**: ...

                ### 🎯 Variation 3: [Social Proof / Direct Theme]
                - **Headline / Hook**: ...
                - **Primary Text**: ...
                - **Call to Action (CTA)**: ...
                """,
                request.getPlatform(), request.getProductName(), request.getDescription(), request.getTargetAudience() != null ? request.getTargetAudience() : "General public");

        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        String result = "";

        try {
            result = aiContentService.askAboutContent(prompt, "System: Marketing Ad Copywriter");
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            logAiRequest(user, "AD_COPY", System.currentTimeMillis() - startTime, success, errorMsg, null);
            incrementUserAiCount(user);
        }

        return result;
    }

    @Override
    public com.aicontentstudio.dto.response.AiDetectResponse detectAiContent(
            com.aicontentstudio.dto.request.AiDetectRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        String systemPrompt = """
                You are a professional linguistic forensics auditor. Analyze the submitted text for perplexity, burstiness, and robotic sentence patterns.
                You MUST return a raw JSON response with:
                - 'score': integer from 0 to 100 (probability that text was AI generated)
                - 'category': one of ('Likely AI-generated' | 'Likely human-written' | 'Mixed content')
                - 'feedback': a beautifully structured Markdown report with bullet points and bold headers detailing:
                  ### 📊 Perplexity & Sentence Variation
                  - Observation...
                  ### 🔍 Vocabulary & Word Choices
                  - Observation...
                  ### 📝 Structural Assessment
                  - Final verdict breakdown...

                Return ONLY raw valid JSON. Do not wrap in backticks.
                """;

        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        String rawResponse = "";

        try {
            rawResponse = aiContentService.askAboutContent(request.getText(), systemPrompt);
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            logAiRequest(user, "AI_DETECT", System.currentTimeMillis() - startTime, success, errorMsg, null);
            incrementUserAiCount(user);
        }

        int score = 30;
        String category = "Mixed content";
        String feedback = "Could not verify content origin.";

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String cleaned = cleanJsonString(rawResponse);
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(cleaned);
            if (root.has("score")) {
                score = root.get("score").asInt();
            }
            if (root.has("category")) {
                category = root.get("category").asText();
            }
            if (root.has("feedback")) {
                feedback = root.get("feedback").asText();
            }
        } catch (Exception e) {
            log.error("Failed to parse AI detector response JSON: {}", rawResponse, e);
            feedback = "Audited analysis output: " + rawResponse;
        }

        return com.aicontentstudio.dto.response.AiDetectResponse.builder()
                .score(score)
                .category(category)
                .feedback(feedback)
                .build();
    }

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String cleaned = raw.trim();
        int firstBrace = cleaned.indexOf("{");
        int lastBrace = cleaned.lastIndexOf("}");
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        } else {
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
        }
        return cleaned.trim();
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return java.util.Arrays.stream(text.split("\\s+"))
                .filter(w -> !w.isBlank())
                .toArray().length;
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
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long usageToday = aiRequestRepository.countByUserAndCreatedAtAfter(user, today);
        if (usageToday >= aiRequestsPerDay) {
            throw new com.aicontentstudio.exception.RateLimitExceededException(aiRequestsPerDay);
        }
    }

    private void incrementUserAiCount(User user) {
        user.setAiRequestsToday(user.getAiRequestsToday() + 1);
        userRepository.save(user);
    }

    private void logAiRequest(User user, String requestType, long responseTimeMs,
                               boolean success, String errorMsg, Blog blog) {
        try {
            String truncatedError = errorMsg;
            if (truncatedError != null && truncatedError.length() > 500) {
                truncatedError = truncatedError.substring(0, 497) + "...";
            }
            AiRequest aiReq = AiRequest.builder()
                    .user(user)
                    .provider(aiContentService.getProviderName())
                    .model(aiContentService.getModelName())
                    .requestType(requestType)
                    .responseTimeMs(responseTimeMs)
                    .success(success)
                    .errorMessage(truncatedError)
                    .promptTokens(0)
                    .completionTokens(0)
                    .totalTokens(0)
                    .blog(blog)
                    .build();
            aiRequestRepository.save(aiReq);
        } catch (Exception e) {
            log.error("Failed to log AI request: {}", e.getMessage());
        }
    }
}
