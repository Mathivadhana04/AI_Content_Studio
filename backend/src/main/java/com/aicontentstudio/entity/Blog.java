package com.aicontentstudio.entity;

import com.aicontentstudio.enums.BlogStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blogs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 300)
    private String metaDescription;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 500)
    private String targetAudience;

    @Column(length = 500)
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BlogStatus status = BlogStatus.DRAFT;

    @Column(length = 50)
    private String tone;

    @Column(nullable = false)
    @Builder.Default
    private int wordCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int seoScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean aiGenerated = false;

    @Column(length = 100)
    private String aiModel;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BlogVersion> versions = new ArrayList<>();

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BlogComment> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
