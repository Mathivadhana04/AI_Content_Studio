package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.ScheduleRequest;
import com.aicontentstudio.dto.response.MessageResponse;
import com.aicontentstudio.entity.ContentSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Blog content scheduling operations.
 */
public interface SchedulerService {

    /**
     * Schedule a blog for future publication.
     * Sets blog.status = SCHEDULED and blog.scheduledAt.
     */
    ContentSchedule scheduleBlog(Long blogId, ScheduleRequest request, String userEmail);

    /**
     * Update the scheduled publication time for an already-scheduled blog.
     */
    ContentSchedule reschedule(Long blogId, ScheduleRequest request, String userEmail);

    /**
     * Cancel the scheduled publication and revert blog.status to DRAFT.
     */
    MessageResponse cancelSchedule(Long blogId, String userEmail);

    /**
     * Return a paginated list of all pending (unpublished) scheduled entries.
     */
    Page<ContentSchedule> getScheduled(Pageable pageable, String userEmail);
}
