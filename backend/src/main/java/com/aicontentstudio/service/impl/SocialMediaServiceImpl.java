package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.SocialPostRequest;
import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.SocialPost;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.SocialPostRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.service.AiContentService;
import com.aicontentstudio.service.SocialMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialMediaServiceImpl implements SocialMediaService {

    private final SocialPostRepository socialPostRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final AiContentService aiContentService;

    @Value("${app.rate-limit.ai-requests-per-day}")
    private int aiRequestsPerDay;

    @Override
    public SocialPost generatePost(SocialPostRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        checkAiRateLimit(user);

        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new ResourceNotFoundException("Blog", request.getBlogId()));
        checkBlogAccess(blog, user);

        log.info("Generating social post for platform: {}, blogId: {}", request.getPlatform(), request.getBlogId());
        String generatedContent = "";
        try {
            generatedContent = aiContentService.generateSocialPost(
                    request.getPlatform().name(),
                    blog.getContent(),
                    blog.getTitle()
            );
        } catch (Exception e) {
            log.error("AI social post generation failed: {}", e.getMessage());
            throw e;
        }

        // Increment user's AI requests
        user.setAiRequestsToday(user.getAiRequestsToday() + 1);
        userRepository.save(user);

        SocialPost socialPost = SocialPost.builder()
                .blog(blog)
                .author(user)
                .platform(request.getPlatform())
                .content(generatedContent)
                .build();

        return socialPostRepository.save(socialPost);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocialPost> getUserPosts(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return socialPostRepository.findByAuthor(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocialPost> getBlogPosts(Long blogId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));
        checkBlogAccess(blog, user);
        return socialPostRepository.findByBlog(blog);
    }

    @Override
    public void deletePost(Long postId, String userEmail) {
        User user = getUserByEmail(userEmail);
        SocialPost post = socialPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("SocialPost", postId));

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to delete this social post");
        }

        socialPostRepository.delete(post);
        log.info("Social post {} deleted by {}", postId, userEmail);
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
}
