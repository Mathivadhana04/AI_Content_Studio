package com.aicontentstudio.repository;

import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.entity.Workspace;
import com.aicontentstudio.enums.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    Page<Blog> findByAuthor(User author, Pageable pageable);

    Page<Blog> findByWorkspace(Workspace workspace, Pageable pageable);

    Page<Blog> findByWorkspaceAndStatus(Workspace workspace, BlogStatus status, Pageable pageable);

    Page<Blog> findByAuthorAndStatus(User author, BlogStatus status, Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.workspace = :workspace AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%',:query,'%')) OR LOWER(b.keywords) LIKE LOWER(CONCAT('%',:query,'%')))")
    Page<Blog> searchInWorkspace(@Param("workspace") Workspace workspace,
                                  @Param("query") String query, Pageable pageable);

    List<Blog> findByStatusAndScheduledAtBefore(BlogStatus status, LocalDateTime dateTime);

    long countByAuthor(User author);

    long countByWorkspaceAndStatus(Workspace workspace, BlogStatus status);

    long countByStatus(BlogStatus status);

    @Query("SELECT SUM(b.wordCount) FROM Blog b WHERE b.author = :author")
    Long sumWordCountByAuthor(@Param("author") User author);

    @Query("SELECT COUNT(b) FROM Blog b WHERE b.author = :author AND b.createdAt >= :since")
    long countByAuthorSince(@Param("author") User author, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(b) FROM Blog b WHERE b.createdAt >= :since")
    long countAllSince(@Param("since") LocalDateTime since);

    @Query("SELECT b.status, COUNT(b) FROM Blog b GROUP BY b.status")
    List<Object[]> countGroupByStatus();
}
