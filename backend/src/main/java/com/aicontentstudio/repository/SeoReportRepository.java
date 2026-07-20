package com.aicontentstudio.repository;

import com.aicontentstudio.entity.SeoReport;
import com.aicontentstudio.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeoReportRepository extends JpaRepository<SeoReport, Long> {

    Optional<SeoReport> findFirstByBlogOrderByAnalyzedAtDesc(Blog blog);

    List<SeoReport> findByBlogOrderByAnalyzedAtDesc(Blog blog);

    @Query("SELECT AVG(sr.overallScore) FROM SeoReport sr WHERE sr.blog.author.id = :userId")
    Optional<Double> findAverageSeoScoreByUserId(@Param("userId") Long userId);
}
