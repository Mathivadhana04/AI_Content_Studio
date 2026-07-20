package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.BlogGenerateRequest;
import com.aicontentstudio.dto.request.BlogUpdateRequest;
import com.aicontentstudio.dto.response.BlogResponse;
import com.aicontentstudio.dto.response.BlogSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogService {
    BlogResponse generateBlog(BlogGenerateRequest request, String userEmail);
    BlogResponse createDraft(BlogUpdateRequest request, Long workspaceId, String userEmail);
    BlogResponse getBlogById(Long id, String userEmail);
    Page<BlogSummaryResponse> getMyBlogs(String userEmail, Pageable pageable);
    Page<BlogSummaryResponse> getWorkspaceBlogs(Long workspaceId, String userEmail, Pageable pageable);
    Page<BlogSummaryResponse> searchBlogs(Long workspaceId, String query, String userEmail, Pageable pageable);
    BlogResponse updateBlog(Long id, BlogUpdateRequest request, String userEmail);
    BlogResponse publishBlog(Long id, String userEmail);
    void deleteBlog(Long id, String userEmail);
    BlogResponse duplicateBlog(Long id, String userEmail);
    BlogResponse saveBlogVersion(Long id, String changeNote, String userEmail);
    java.util.List<com.aicontentstudio.dto.response.BlogVersionResponse> getBlogVersions(Long id, String userEmail);
}
