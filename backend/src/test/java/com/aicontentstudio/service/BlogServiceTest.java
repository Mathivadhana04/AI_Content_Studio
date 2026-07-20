package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.BlogGenerateRequest;
import com.aicontentstudio.dto.response.BlogResponse;
import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.entity.Workspace;
import com.aicontentstudio.enums.AiTone;
import com.aicontentstudio.enums.BlogStatus;
import com.aicontentstudio.repository.AiRequestRepository;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.repository.WorkspaceRepository;
import com.aicontentstudio.service.impl.BlogServiceImpl;
import com.aicontentstudio.repository.WorkspaceMemberRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private AiRequestRepository aiRequestRepository;

    @Mock
    private AiContentService aiContentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BlogServiceImpl blogService;

    private User user;
    private Workspace workspace;
    private Blog blog;
    private BlogGenerateRequest generateRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(blogService, "aiRequestsPerDay", 50);
        user = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .aiRequestsToday(0)
                .build();

        workspace = Workspace.builder()
                .id(1L)
                .name("Default Workspace")
                .owner(user)
                .build();

        blog = Blog.builder()
                .id(1L)
                .title("A Guide to Spring Boot")
                .content("# A Guide to Spring Boot\n**Meta Description:** Intro here\nBody content goes here.")
                .author(user)
                .workspace(workspace)
                .status(BlogStatus.DRAFT)
                .aiGenerated(true)
                .build();

        generateRequest = new BlogGenerateRequest();
        generateRequest.setTopic("A Guide to Spring Boot");
        generateRequest.setTargetAudience("Developers");
        generateRequest.setTone(AiTone.PROFESSIONAL);
        generateRequest.setKeywords("Spring Boot, Java");
        generateRequest.setTargetWordCount(500);
        generateRequest.setWorkspaceId(1L);
    }

    @Test
    void generateBlog_Success() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(aiRequestRepository.countByUserAndCreatedAtAfter(any(), any())).thenReturn(0L);
        when(workspaceRepository.findById(any())).thenReturn(Optional.of(workspace));
        when(aiContentService.generateBlog(any(), any(), any(), any(), anyInt()))
                .thenReturn("# A Guide to Spring Boot\n**Meta Description:** Intro here\nBody content goes here.");
        when(aiContentService.getModelName()).thenReturn("llama-3.3-70b");
        when(blogRepository.save(any())).thenReturn(blog);

        BlogResponse response = blogService.generateBlog(generateRequest, "john.doe@example.com");

        assertNotNull(response);
        assertEquals("A Guide to Spring Boot", response.getTitle());
        assertTrue(response.isAiGenerated());
        verify(blogRepository, times(1)).save(any());
        verify(notificationService, times(1)).createAndPushNotification(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getBlogById_Success() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(blogRepository.findById(any())).thenReturn(Optional.of(blog));

        BlogResponse response = blogService.getBlogById(1L, "john.doe@example.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }
}
