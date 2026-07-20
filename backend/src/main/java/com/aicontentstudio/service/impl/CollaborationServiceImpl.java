package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.CommentRequest;
import com.aicontentstudio.entity.*;
import com.aicontentstudio.enums.NotificationType;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.*;
import com.aicontentstudio.service.CollaborationService;
import com.aicontentstudio.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CollaborationServiceImpl implements CollaborationService {

    private final BlogCommentRepository blogCommentRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final NotificationService notificationService;

    @Override
    public BlogComment addComment(Long blogId, CommentRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogAccess(blog, user);

        BlogComment comment = BlogComment.builder()
                .blog(blog)
                .author(user)
                .content(request.getContent())
                .mentionedUserIds(request.getMentionedUserIds() != null
                        ? request.getMentionedUserIds().stream().map(String::valueOf).collect(Collectors.joining(","))
                        : null)
                .resolved(false)
                .build();

        BlogComment saved = blogCommentRepository.save(comment);

        // Audit log comment action
        logActivity(user, "ADD_COMMENT", "Blog", blogId, "Added comment on blog: " + blog.getTitle());

        // Process mentions
        if (request.getMentionedUserIds() != null) {
            for (Long userId : request.getMentionedUserIds()) {
                userRepository.findById(userId).ifPresent(mentionedUser -> {
                    notificationService.createAndPushNotification(
                            mentionedUser, user, NotificationType.MENTION,
                            "You were mentioned",
                            user.getFullName() + " mentioned you in a comment on \"" + blog.getTitle() + "\"",
                            "/editor/" + blogId,
                            blogId, "Blog"
                    );
                });
            }
        }

        // Notify blog author if comment not by author
        if (!blog.getAuthor().getId().equals(user.getId())) {
            notificationService.createAndPushNotification(
                    blog.getAuthor(), user, NotificationType.COMMENT,
                    "New comment on your blog",
                    user.getFullName() + " commented on \"" + blog.getTitle() + "\"",
                    "/editor/" + blogId,
                    blogId, "Blog"
            );
        }

        log.info("Comment {} added to blog {} by {}", saved.getId(), blogId, userEmail);
        return saved;
    }

    @Override
    public BlogComment replyToComment(Long commentId, CommentRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        BlogComment parent = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("BlogComment", commentId));

        Blog blog = parent.getBlog();
        checkBlogAccess(blog, user);

        BlogComment reply = BlogComment.builder()
                .blog(blog)
                .author(user)
                .parent(parent)
                .content(request.getContent())
                .resolved(false)
                .build();

        BlogComment saved = blogCommentRepository.save(reply);

        // Notify parent comment author if not self
        if (!parent.getAuthor().getId().equals(user.getId())) {
            notificationService.createAndPushNotification(
                    parent.getAuthor(), user, NotificationType.COMMENT,
                    "New reply to your comment",
                    user.getFullName() + " replied to your comment on \"" + blog.getTitle() + "\"",
                    "/editor/" + blog.getId(),
                    blog.getId(), "Blog"
            );
        }

        log.info("Reply {} added to comment {} by {}", saved.getId(), commentId, userEmail);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogComment> getComments(Long blogId, Pageable pageable, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogAccess(blog, user);
        return blogCommentRepository.findByBlogAndParentIsNull(blog, pageable);
    }

    @Override
    public void deleteComment(Long commentId, String userEmail) {
        User user = getUserByEmail(userEmail);
        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("BlogComment", commentId));

        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isBlogAuthor = comment.getBlog().getAuthor().getId().equals(user.getId());
        boolean isWorkspaceOwner = comment.getBlog().getWorkspace().getOwner().getId().equals(user.getId());

        if (!isAuthor && !isBlogAuthor && !isWorkspaceOwner) {
            throw new UnauthorizedException("You do not have permission to delete this comment");
        }

        blogCommentRepository.delete(comment);
        log.info("Comment {} deleted by {}", commentId, userEmail);
    }

    @Override
    public BlogComment resolveComment(Long commentId, String userEmail) {
        User user = getUserByEmail(userEmail);
        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("BlogComment", commentId));

        checkBlogAccess(comment.getBlog(), user);

        comment.setResolved(true);
        BlogComment saved = blogCommentRepository.save(comment);

        log.info("Comment {} resolved by {}", commentId, userEmail);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLog> getActivityLog(Long blogId, Pageable pageable, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogAccess(blog, user);
        return activityLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("Blog", blogId, pageable);
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

    private void logActivity(User user, String action, String entityType, Long entityId, String description) {
        try {
            ActivityLog logEntity = ActivityLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .build();
            activityLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Failed to write activity log: {}", e.getMessage());
        }
    }
}
