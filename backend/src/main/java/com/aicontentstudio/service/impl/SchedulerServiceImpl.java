package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.request.ScheduleRequest;
import com.aicontentstudio.dto.response.MessageResponse;
import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.ContentSchedule;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.enums.BlogStatus;
import com.aicontentstudio.enums.NotificationType;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.ContentScheduleRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.service.NotificationService;
import com.aicontentstudio.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchedulerServiceImpl implements SchedulerService {

    private final ContentScheduleRepository contentScheduleRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public ContentSchedule scheduleBlog(Long blogId, ScheduleRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogWriteAccess(blog, user);

        if (blog.getStatus() == BlogStatus.PUBLISHED) {
            throw new BadRequestException("Blog is already published");
        }

        if (contentScheduleRepository.existsByBlogAndPublishedFalse(blog)) {
            throw new BadRequestException("Blog is already scheduled for publication");
        }

        blog.setStatus(BlogStatus.SCHEDULED);
        blog.setScheduledAt(request.getScheduledAt());
        blogRepository.save(blog);

        ContentSchedule schedule = ContentSchedule.builder()
                .blog(blog)
                .scheduledBy(user)
                .scheduledAt(request.getScheduledAt())
                .notes(request.getNotes())
                .published(false)
                .build();

        ContentSchedule saved = contentScheduleRepository.save(schedule);

        // Notify user
        notificationService.createAndPushNotification(
                user, user, NotificationType.BLOG_SCHEDULED,
                "Blog Scheduled",
                "\"" + blog.getTitle() + "\" is scheduled to publish at " + request.getScheduledAt().toString(),
                "/scheduler",
                saved.getId(), "ContentSchedule"
        );

        log.info("Blog {} scheduled for publication at {} by {}", blogId, request.getScheduledAt(), userEmail);
        return saved;
    }

    @Override
    public ContentSchedule reschedule(Long blogId, ScheduleRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogWriteAccess(blog, user);

        ContentSchedule schedule = contentScheduleRepository.findByBlog(blog)
                .orElseThrow(() -> new BadRequestException("No active schedule found for this blog"));

        if (schedule.isPublished()) {
            throw new BadRequestException("Cannot reschedule a published blog");
        }

        blog.setScheduledAt(request.getScheduledAt());
        blogRepository.save(blog);

        schedule.setScheduledAt(request.getScheduledAt());
        if (request.getNotes() != null) {
            schedule.setNotes(request.getNotes());
        }
        ContentSchedule updated = contentScheduleRepository.save(schedule);

        log.info("Blog {} rescheduled to {} by {}", blogId, request.getScheduledAt(), userEmail);
        return updated;
    }

    @Override
    public MessageResponse cancelSchedule(Long blogId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogWriteAccess(blog, user);

        ContentSchedule schedule = contentScheduleRepository.findByBlog(blog)
                .orElseThrow(() -> new BadRequestException("No schedule found for this blog"));

        if (schedule.isPublished()) {
            throw new BadRequestException("Cannot cancel schedule for a published blog");
        }

        contentScheduleRepository.delete(schedule);

        blog.setStatus(BlogStatus.DRAFT);
        blog.setScheduledAt(null);
        blogRepository.save(blog);

        log.info("Schedule for blog {} cancelled by {}", blogId, userEmail);
        return MessageResponse.of("Blog schedule cancelled successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentSchedule> getScheduled(Pageable pageable, String userEmail) {
        User user = getUserByEmail(userEmail);
        // Note: For now return all unpublished schedules. We can filter by user/workspace if needed.
        return contentScheduleRepository.findByPublished(false, pageable);
    }

    // ===== Helpers =====
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void checkBlogWriteAccess(Blog blog, User user) {
        boolean isAuthor = blog.getAuthor().getId().equals(user.getId());
        boolean isWorkspaceOwner = blog.getWorkspace().getOwner().getId().equals(user.getId());
        if (!isAuthor && !isWorkspaceOwner) {
            throw new UnauthorizedException("You do not have permission to modify this blog schedule");
        }
    }
}
