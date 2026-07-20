package com.aicontentstudio.service.impl;

import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.HeadlineVariant;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.HeadlineVariantRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.service.AiContentService;
import com.aicontentstudio.service.HeadlineService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HeadlineServiceImpl implements HeadlineService {

    private final BlogRepository blogRepository;
    private final HeadlineVariantRepository headlineVariantRepository;
    private final UserRepository userRepository;
    private final AiContentService aiContentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<HeadlineVariant> generateHeadlines(Long blogId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogAccess(blog, user);

        String topic = blog.getTitle();
        String content = blog.getContent() != null ? blog.getContent() : "";

        log.info("Generating headlines for blog: {}", blogId);
        String jsonResponse = aiContentService.generateHeadlines(topic, content);

        List<HeadlineVariant> variants = new ArrayList<>();
        try {
            // Delete old variants
            headlineVariantRepository.deleteByBlog(blog);

            JsonNode root = objectMapper.readTree(cleanJsonString(jsonResponse));
            String[] types = {"seo", "professional", "clickbait", "linkedin"};

            for (String type : types) {
                if (root.has(type)) {
                    HeadlineVariant variant = HeadlineVariant.builder()
                            .blog(blog)
                            .headline(root.get(type).asText())
                            .variant(type)
                            .selected(false)
                            .build();
                    variants.add(headlineVariantRepository.save(variant));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse headlines JSON: {}", e.getMessage(), e);
            // Fallback generation logic if JSON fails to parse
            String[] types = {"seo", "professional", "clickbait", "linkedin"};
            for (String type : types) {
                HeadlineVariant variant = HeadlineVariant.builder()
                        .blog(blog)
                        .headline(topic + " (" + type.toUpperCase() + " Mode)")
                        .variant(type)
                        .selected(false)
                        .build();
                variants.add(headlineVariantRepository.save(variant));
            }
        }

        return variants;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeadlineVariant> getHeadlines(Long blogId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogAccess(blog, user);
        return headlineVariantRepository.findByBlogOrderByCreatedAtDesc(blog);
    }

    @Override
    public String selectHeadline(Long variantId, String userEmail) {
        User user = getUserByEmail(userEmail);
        HeadlineVariant variant = headlineVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("HeadlineVariant", variantId));

        Blog blog = variant.getBlog();
        checkBlogAccess(blog, user);

        // Reset other variants
        List<HeadlineVariant> allVariants = headlineVariantRepository.findByBlogOrderByCreatedAtDesc(blog);
        for (HeadlineVariant v : allVariants) {
            v.setSelected(v.getId().equals(variantId));
            headlineVariantRepository.save(v);
        }

        // Update blog title
        blog.setTitle(variant.getHeadline());
        blogRepository.save(blog);

        log.info("Selected headline '{}' for blog {}", variant.getHeadline(), blog.getId());
        return variant.getHeadline();
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

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
