package com.aicontentstudio.repository;

import com.aicontentstudio.entity.SocialPost;
import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.enums.ContentPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {

    Page<SocialPost> findByAuthor(User author, Pageable pageable);

    List<SocialPost> findByBlog(Blog blog);

    List<SocialPost> findByBlogAndPlatform(Blog blog, ContentPlatform platform);
}
