package com.aicontentstudio.repository;

import com.aicontentstudio.entity.BlogComment;
import com.aicontentstudio.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {

    Page<BlogComment> findByBlogAndParentIsNull(Blog blog, Pageable pageable);

    List<BlogComment> findByParentOrderByCreatedAtAsc(BlogComment parent);

    long countByBlog(Blog blog);
}
