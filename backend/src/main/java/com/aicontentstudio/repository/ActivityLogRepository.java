package com.aicontentstudio.repository;

import com.aicontentstudio.entity.ActivityLog;
import com.aicontentstudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId, Pageable pageable);

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
