package com.aicontentstudio.blog.service;

import com.aicontentstudio.blog.dto.BlogRequest;
import com.aicontentstudio.blog.dto.BlogResponse;
import java.util.List;

public interface BlogService {

    BlogResponse create(BlogRequest request);

    BlogResponse update(Long id, BlogRequest request);

    BlogResponse getById(Long id);

    List<BlogResponse> getAll();

    List<BlogResponse> getByWorkspace(Long workspace);

    void delete(Long id);
}