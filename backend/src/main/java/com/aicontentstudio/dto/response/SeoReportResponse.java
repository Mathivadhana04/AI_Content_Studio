package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.SeoReport;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SeoReportResponse {
    private Long id;
    private Long blogId;
    private int overallScore;
    private int keywordDensityScore;
    private int readabilityScore;
    private int headingStructureScore;
    private int metaDescriptionScore;
    private int contentLengthScore;
    private String recommendations;
    private String keywordAnalysis;
    private String headingAnalysis;
    private LocalDateTime analyzedAt;

    public static SeoReportResponse fromEntity(SeoReport report) {
        return SeoReportResponse.builder()
                .id(report.getId())
                .blogId(report.getBlog().getId())
                .overallScore(report.getOverallScore())
                .keywordDensityScore(report.getKeywordDensityScore())
                .readabilityScore(report.getReadabilityScore())
                .headingStructureScore(report.getHeadingStructureScore())
                .metaDescriptionScore(report.getMetaDescriptionScore())
                .contentLengthScore(report.getContentLengthScore())
                .recommendations(report.getRecommendations())
                .keywordAnalysis(report.getKeywordAnalysis())
                .headingAnalysis(report.getHeadingAnalysis())
                .analyzedAt(report.getAnalyzedAt())
                .build();
    }
}
