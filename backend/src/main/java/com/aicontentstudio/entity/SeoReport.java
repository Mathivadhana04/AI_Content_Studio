package com.aicontentstudio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seo_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SeoReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(nullable = false)
    private int overallScore;

    @Column(nullable = false)
    private int keywordDensityScore;

    @Column(nullable = false)
    private int readabilityScore;

    @Column(nullable = false)
    private int headingStructureScore;

    @Column(nullable = false)
    private int metaDescriptionScore;

    @Column(nullable = false)
    private int contentLengthScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String keywordAnalysis;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String headingAnalysis;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;
}
