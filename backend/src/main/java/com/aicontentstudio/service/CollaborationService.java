package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.CommentRequest;
import com.aicontentstudio.entity.ActivityLog;
import com.aicontentstudio.entity.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CollaborationService {

    BlogComment addComment(Long blogId, CommentRequest request, String userEmail);

    BlogComment replyToComment(Long commentId, CommentRequest request, String userEmail);

    Page<BlogComment> getComments(Long blogId, Pageable pageable, String userEmail);

    void deleteComment(Long commentId, String userEmail);

    BlogComment resolveComment(Long commentId, String userEmail);

    Page<ActivityLog> getActivityLog(Long blogId, Pageable pageable, String userEmail);
}
