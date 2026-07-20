package com.aicontentstudio.entity;

import com.aicontentstudio.enums.EmailType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_campaigns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class EmailCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog sourceBlog;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailType emailType;

    @Column(nullable = false, length = 255)
    private String subject;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String htmlContent;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String plainTextContent;

    @Column(length = 255)
    private String targetAudience;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
