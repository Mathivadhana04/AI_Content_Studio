package com.aicontentstudio.repository;

import com.aicontentstudio.entity.BlogVersion;
import com.aicontentstudio.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogVersionRepository extends JpaRepository<BlogVersion, Long> {

    List<BlogVersion> findByBlogOrderByVersionNumberDesc(Blog blog);

    Optional<BlogVersion> findByBlogAndVersionNumber(Blog blog, int versionNumber);

    @Query("SELECT MAX(bv.versionNumber) FROM BlogVersion bv WHERE bv.blog = :blog")
    Optional<Integer> findMaxVersionNumberByBlog(@Param("blog") Blog blog);

    long countByBlog(Blog blog);
}
