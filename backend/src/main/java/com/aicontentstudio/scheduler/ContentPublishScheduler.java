package com.aicontentstudio.scheduler;

import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.ContentSchedule;
import com.aicontentstudio.enums.BlogStatus;
import com.aicontentstudio.enums.NotificationType;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.ContentScheduleRepository;
import com.aicontentstudio.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentPublishScheduler {

    private final ContentScheduleRepository contentScheduleRepository;
    private final BlogRepository blogRepository;
    private final NotificationService notificationService;

    /**
     * Run every 60 seconds. Checks for blogs that are scheduled to be published
     * and publishes them if their scheduled time has arrived.
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void publishScheduledBlogs() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("Checking for scheduled blogs to publish at: {}", now);

        List<ContentSchedule> pendingSchedules = contentScheduleRepository
                .findByPublishedFalseAndScheduledAtBefore(now);

        if (pendingSchedules.isEmpty()) {
            return;
        }

        log.info("Found {} scheduled blogs to publish", pendingSchedules.size());

        for (ContentSchedule schedule : pendingSchedules) {
            try {
                Blog blog = schedule.getBlog();
                blog.setStatus(BlogStatus.PUBLISHED);
                blog.setPublishedAt(now);
                blogRepository.save(blog);

                schedule.setPublished(true);
                schedule.setPublishedAt(now);
                contentScheduleRepository.save(schedule);

                // Send push notification to the author
                notificationService.createAndPushNotification(
                        blog.getAuthor(), blog.getAuthor(), NotificationType.BLOG_PUBLISHED,
                        "Blog Published Automatically",
                        "Your scheduled blog \"" + blog.getTitle() + "\" has been published automatically.",
                        "/blogs/" + blog.getId(),
                        blog.getId(), "Blog"
                );

                log.info("Successfully published scheduled blog: {} (ID: {})", blog.getTitle(), blog.getId());

            } catch (Exception e) {
                log.error("Failed to publish scheduled blog with schedule ID: {}", schedule.getId(), e);
            }
        }
    }
}
