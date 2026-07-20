package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.BlogGenerateRequest;
import com.aicontentstudio.dto.request.BlogUpdateRequest;
import com.aicontentstudio.dto.response.BlogResponse;
import com.aicontentstudio.dto.response.BlogSummaryResponse;
import com.aicontentstudio.entity.*;
import com.aicontentstudio.enums.BlogStatus;
import com.aicontentstudio.enums.NotificationType;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.*;
import com.aicontentstudio.service.AiContentService;
import com.aicontentstudio.service.BlogService;
import com.aicontentstudio.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogVersionRepository blogVersionRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final AiRequestRepository aiRequestRepository;
    private final AiContentService aiContentService;
    private final NotificationService notificationService;

    @Value("${app.rate-limit.ai-requests-per-day}")
    private int aiRequestsPerDay;

    @Override
    public BlogResponse generateBlog(BlogGenerateRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        Workspace workspace = request.getWorkspaceId() != null
                ? workspaceRepository.findById(request.getWorkspaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Workspace", request.getWorkspaceId()))
                : getOrCreateDefaultWorkspace(user);

        checkWorkspaceAccess(workspace, user);

        log.info("Generating blog for user: {}, topic: {}", userEmail, request.getTopic());
        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;
        String generatedContent = "";

        try {
            generatedContent = aiContentService.generateBlog(
                    request.getTopic(),
                    request.getTargetAudience(),
                    request.getTone(),
                    request.getKeywords(),
                    request.getTargetWordCount());
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            logAiRequest(user, "BLOG_GENERATION", responseTime, success, errorMsg, null);
            incrementUserAiCount(user);
        }

        // Extract title from the generated content (first H1)
        String title = extractTitle(generatedContent, request.getTopic());
        if (title != null && title.length() > 255) {
            title = title.substring(0, 252) + "...";
        }
        String metaDesc = extractMetaDescription(generatedContent);
        if (metaDesc != null && metaDesc.length() > 300) {
            metaDesc = metaDesc.substring(0, 297) + "...";
        }
        int wordCount = countWords(generatedContent);

        Blog blog = Blog.builder()
                .title(title)
                .metaDescription(metaDesc)
                .content(generatedContent)
                .targetAudience(request.getTargetAudience())
                .keywords(request.getKeywords())
                .tone(request.getTone().name().toLowerCase())
                .status(BlogStatus.DRAFT)
                .wordCount(wordCount)
                .aiGenerated(true)
                .aiModel(aiContentService.getModelName())
                .author(user)
                .workspace(workspace)
                .build();

        Blog saved = blogRepository.save(blog);

        // Send notification
        notificationService.createAndPushNotification(
                user, user, NotificationType.BLOG_GENERATED,
                "Blog Generated!",
                "\"" + title + "\" has been generated successfully.",
                "/editor/" + saved.getId(),
                saved.getId(), "Blog");

        log.info("Blog generated successfully: id={}, words={}", saved.getId(), wordCount);
        return BlogResponse.fromEntity(saved);
    }

    @Override
    public BlogResponse createDraft(BlogUpdateRequest request, Long workspaceId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId));
        checkWorkspaceAccess(workspace, user);

        Blog blog = Blog.builder()
                .title(request.getTitle() != null ? request.getTitle() : "Untitled Blog")
                .content(request.getContent() != null ? request.getContent() : "")
                .metaDescription(request.getMetaDescription())
                .keywords(request.getKeywords())
                .status(BlogStatus.DRAFT)
                .wordCount(request.getContent() != null ? countWords(request.getContent()) : 0)
                .author(user)
                .workspace(workspace)
                .build();

        return BlogResponse.fromEntity(blogRepository.save(blog));
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponse getBlogById(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
        checkBlogReadAccess(blog, user);
        return BlogResponse.fromEntity(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogSummaryResponse> getMyBlogs(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return blogRepository.findByAuthor(user, pageable).map(BlogSummaryResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogSummaryResponse> getWorkspaceBlogs(Long workspaceId, String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId));
        checkWorkspaceAccess(workspace, user);
        return blogRepository.findByWorkspace(workspace, pageable).map(BlogSummaryResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogSummaryResponse> searchBlogs(Long workspaceId, String query, String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", workspaceId));
        checkWorkspaceAccess(workspace, user);
        return blogRepository.searchInWorkspace(workspace, query, pageable).map(BlogSummaryResponse::fromEntity);
    }

    @Override
    public BlogResponse updateBlog(Long id, BlogUpdateRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
        checkBlogWriteAccess(blog, user);

        if (request.getTitle() != null) blog.setTitle(request.getTitle());
        if (request.getMetaDescription() != null) blog.setMetaDescription(request.getMetaDescription());
        if (request.getContent() != null) {
            blog.setContent(request.getContent());
            blog.setWordCount(countWords(request.getContent()));
        }
        if (request.getKeywords() != null) blog.setKeywords(request.getKeywords());
        if (request.getStatus() != null) blog.setStatus(request.getStatus());
        if (request.getScheduledAt() != null) blog.setScheduledAt(request.getScheduledAt());

        return BlogResponse.fromEntity(blogRepository.save(blog));
    }

    @Override
    public BlogResponse publishBlog(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
        checkBlogWriteAccess(blog, user);

        blog.setStatus(BlogStatus.PUBLISHED);
        blog.setPublishedAt(LocalDateTime.now());
        Blog saved = blogRepository.save(blog);

        notificationService.createAndPushNotification(
                user, user, NotificationType.BLOG_PUBLISHED,
                "Blog Published!",
                "\"" + blog.getTitle() + "\" is now live.",
                "/blogs/" + id,
                id, "Blog");

        return BlogResponse.fromEntity(saved);
    }

    @Override
    public void deleteBlog(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
        checkBlogWriteAccess(blog, user);
        blogRepository.delete(blog);
        log.info("Blog {} deleted by {}", id, userEmail);
    }

    @Override
    public BlogResponse duplicateBlog(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog original = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
        checkBlogReadAccess(original, user);

        Blog duplicate = Blog.builder()
                .title("[Copy] " + original.getTitle())
                .metaDescription(original.getMetaDescription())
                .content(original.getContent())
                .keywords(original.getKeywords())
                .targetAudience(original.getTargetAudience())
                .tone(original.getTone())
                .status(BlogStatus.DRAFT)
                .wordCount(original.getWordCount())
                .aiGenerated(original.isAiGenerated())
                .author(user)
                .workspace(original.getWorkspace())
                .build();

        return BlogResponse.fromEntity(blogRepository.save(duplicate));
    }

    @Override
    public BlogResponse saveBlogVersion(Long id, String changeNote, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
        checkBlogWriteAccess(blog, user);

        int nextVersion = blogVersionRepository.findMaxVersionNumberByBlog(blog).orElse(0) + 1;

        BlogVersion version = BlogVersion.builder()
                .blog(blog)
                .savedBy(user)
                .title(blog.getTitle())
                .content(blog.getContent())
                .versionNumber(nextVersion)
                .changeNote(changeNote)
                .build();

        blogVersionRepository.save(version);
        return BlogResponse.fromEntity(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.aicontentstudio.dto.response.BlogVersionResponse> getBlogVersions(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
        checkBlogReadAccess(blog, user);
        return blogVersionRepository.findByBlogOrderByVersionNumberDesc(blog).stream()
                .map(com.aicontentstudio.dto.response.BlogVersionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ===== Helpers =====
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Workspace getOrCreateDefaultWorkspace(User user) {
        return workspaceRepository.findByOwner(user, Pageable.unpaged())
                .stream().findFirst()
                .orElseGet(() -> {
                    Workspace ws = Workspace.builder()
                            .name(user.getFirstName() + "'s Workspace")
                            .owner(user)
                            .build();
                    return workspaceRepository.save(ws);
                });
    }

    private void checkWorkspaceAccess(Workspace workspace, User user) {
        boolean isOwner = workspace.getOwner().getId().equals(user.getId());
        boolean isMember = workspaceMemberRepository.existsByWorkspaceAndUser(workspace, user);
        if (!isOwner && !isMember) {
            throw new UnauthorizedException("You don't have access to this workspace");
        }
    }

    private void checkBlogReadAccess(Blog blog, User user) {
        checkWorkspaceAccess(blog.getWorkspace(), user);
    }

    private void checkBlogWriteAccess(Blog blog, User user) {
        boolean isAuthor = blog.getAuthor().getId().equals(user.getId());
        boolean isWorkspaceOwner = blog.getWorkspace().getOwner().getId().equals(user.getId());
        if (!isAuthor && !isWorkspaceOwner) {
            throw new UnauthorizedException("You don't have permission to modify this blog");
        }
    }

    private void checkAiRateLimit(User user) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long usageToday = aiRequestRepository.countByUserAndCreatedAtAfter(user, today);
        if (usageToday >= aiRequestsPerDay) {
            throw new com.aicontentstudio.exception.RateLimitExceededException(aiRequestsPerDay);
        }
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
                    .promptTokens(0) // Groq free tier doesn't always return token counts easily
                    .completionTokens(0)
                    .totalTokens(0)
                    .blog(blog)
                    .build();
            aiRequestRepository.save(aiReq);
        } catch (Exception e) {
            log.error("Failed to log AI request: {}", e.getMessage());
        }
    }

    private void incrementUserAiCount(User user) {
        user.setAiRequestsToday(user.getAiRequestsToday() + 1);
        userRepository.save(user);
    }

    private String extractTitle(String content, String fallback) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ")) {
                return trimmed.substring(2).trim();
            }
        }
        return fallback;
    }

    private String extractMetaDescription(String content) {
        if (content.contains("**Meta Description:**")) {
            int start = content.indexOf("**Meta Description:**") + "**Meta Description:**".length();
            int end = content.indexOf("\n", start);
            if (end == -1) end = Math.min(start + 200, content.length());
            return content.substring(start, end).trim();
        }
        // Fallback: take first meaningful paragraph
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("*")
                    && trimmed.length() > 50) {
                return trimmed.substring(0, Math.min(trimmed.length(), 160));
            }
        }
        return "";
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return Arrays.stream(text.split("\\s+"))
                .filter(w -> !w.isBlank())
                .collect(Collectors.toList()).size();
    }
}
