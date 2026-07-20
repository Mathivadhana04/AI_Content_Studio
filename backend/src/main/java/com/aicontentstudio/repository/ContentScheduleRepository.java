package com.aicontentstudio.repository;

import com.aicontentstudio.entity.ContentSchedule;
import com.aicontentstudio.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContentScheduleRepository extends JpaRepository<ContentSchedule, Long> {

    Optional<ContentSchedule> findByBlog(Blog blog);

    List<ContentSchedule> findByPublishedFalseAndScheduledAtBefore(LocalDateTime dateTime);

    Page<ContentSchedule> findByPublished(boolean published, Pageable pageable);

    boolean existsByBlogAndPublishedFalse(Blog blog);
}
