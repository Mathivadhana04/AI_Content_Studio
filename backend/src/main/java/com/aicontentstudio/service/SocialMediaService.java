package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.SocialPostRequest;
import com.aicontentstudio.entity.SocialPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * AI-powered social media post generation and management.
 */
public interface SocialMediaService {

    /**
     * Generate a platform-specific post from a blog using the AI service.
     * Persists and returns the created SocialPost entity.
     */
    SocialPost generatePost(SocialPostRequest request, String userEmail);

    /**
     * Paginated list of all social posts created by the given user.
     */
    Page<SocialPost> getUserPosts(String userEmail, Pageable pageable);

    /**
     * All social posts derived from a specific blog.
     */
    List<SocialPost> getBlogPosts(Long blogId, String userEmail);

    /**
     * Delete a social post owned by the user.
     */
    void deletePost(Long postId, String userEmail);
}
