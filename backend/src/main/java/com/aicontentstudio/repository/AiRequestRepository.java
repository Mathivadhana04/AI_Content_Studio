package com.aicontentstudio.repository;

import com.aicontentstudio.entity.AiRequest;
import com.aicontentstudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AiRequestRepository extends JpaRepository<AiRequest, Long> {

    Page<AiRequest> findByUser(User user, Pageable pageable);

    long countByUserAndCreatedAtAfter(User user, LocalDateTime after);

    @Query("SELECT SUM(ar.totalTokens) FROM AiRequest ar WHERE ar.user = :user AND ar.createdAt >= :since")
    Long sumTokensByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(ar) FROM AiRequest ar WHERE ar.createdAt >= :since")
    long countAllSince(@Param("since") LocalDateTime since);

    @Query("SELECT ar.requestType, COUNT(ar) FROM AiRequest ar GROUP BY ar.requestType ORDER BY COUNT(ar) DESC")
    java.util.List<Object[]> countGroupByRequestType();
}
