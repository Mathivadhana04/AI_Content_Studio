package com.aicontentstudio.repository;

import com.aicontentstudio.entity.HeadlineVariant;
import com.aicontentstudio.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeadlineVariantRepository extends JpaRepository<HeadlineVariant, Long> {

    List<HeadlineVariant> findByBlogOrderByCreatedAtDesc(Blog blog);

    void deleteByBlog(Blog blog);
}
