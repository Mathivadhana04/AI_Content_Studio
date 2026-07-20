package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.response.SeoReportResponse;
import com.aicontentstudio.entity.Blog;
import com.aicontentstudio.entity.SeoReport;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.exception.BadRequestException;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.BlogRepository;
import com.aicontentstudio.repository.SeoReportRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.service.SeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeoServiceImpl implements SeoService {

    private final BlogRepository blogRepository;
    private final SeoReportRepository seoReportRepository;
    private final UserRepository userRepository;

    @Override
    public SeoReportResponse analyzeBlog(Long blogId, String userEmail, String targetKeyword) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogAccess(blog, user);

        String content = blog.getContent() != null ? blog.getContent() : "";
        String title = blog.getTitle() != null ? blog.getTitle() : "";
        String metaDesc = blog.getMetaDescription() != null ? blog.getMetaDescription() : "";

        int totalWords = countWords(content);

        // 1. Content Length Score
        int contentLengthScore = 40;
        if (totalWords >= 1000) {
            contentLengthScore = 100;
        } else if (totalWords >= 500) {
            contentLengthScore = 70;
        }

        // 2. Meta Description Score
        int metaLength = metaDesc.length();
        int metaDescriptionScore = 40;
        if (metaLength >= 150 && metaLength <= 160) {
            metaDescriptionScore = 100;
        } else if (metaLength >= 120 && metaLength < 150) {
            metaDescriptionScore = 80;
        } else if (metaLength > 160 && metaLength <= 180) {
            metaDescriptionScore = 80;
        } else if (metaLength > 0) {
            metaDescriptionScore = 50;
        }

        // 3. Keyword Density Score
        int keywordCount = 0;
        double keywordDensity = 0.0;
        int keywordDensityScore = 0;
        String keywordAnalysisText = "No target keyword specified.";

        if (targetKeyword != null && !targetKeyword.isBlank()) {
            keywordCount = countKeywordMatches(content, targetKeyword);
            if (totalWords > 0) {
                keywordDensity = ((double) keywordCount / totalWords) * 100;
            }
            if (keywordDensity >= 1.0 && keywordDensity <= 3.0) {
                keywordDensityScore = 100;
            } else if (keywordDensity > 0.0 && keywordDensity < 1.0) {
                keywordDensityScore = (int) (keywordDensity * 100);
            } else if (keywordDensity > 3.0 && keywordDensity <= 5.0) {
                keywordDensityScore = 80;
            } else if (keywordDensity > 5.0) {
                keywordDensityScore = 40;
            }
            keywordAnalysisText = String.format("Target keyword '%s' found %d times (Density: %.2f%%). Ideal density is 1.0%% to 3.0%%.",
                    targetKeyword, keywordCount, keywordDensity);
        }

        // 4. Readability Score (based on average sentence length)
        int readabilityScore = 100;
        int sentenceCount = countSentences(content);
        double avgSentenceLength = 0;
        if (sentenceCount > 0 && totalWords > 0) {
            avgSentenceLength = (double) totalWords / sentenceCount;
            if (avgSentenceLength < 15) {
                readabilityScore = 100;
            } else if (avgSentenceLength < 20) {
                readabilityScore = 90;
            } else if (avgSentenceLength < 25) {
                readabilityScore = 70;
            } else {
                readabilityScore = 40;
            }
        }

        // 5. Heading Structure Score
        boolean hasH1 = content.contains("\n# ") || content.startsWith("# ") || title.length() > 0;
        boolean hasH2 = content.contains("\n## ") || content.contains("## ");
        boolean hasH3 = content.contains("\n### ") || content.contains("### ");

        int headingStructureScore = 20;
        if (hasH1 && hasH2 && hasH3) {
            headingStructureScore = 100;
        } else if (hasH1 && hasH2) {
            headingStructureScore = 80;
        } else if (hasH1) {
            headingStructureScore = 50;
        }

        String headingAnalysisText = String.format("H1 Present: %b, H2 Present: %b, H3 Present: %b.", hasH1, hasH2, hasH3);

        // Overall Score (Weighted Average)
        int overallScore = (int) (
                (contentLengthScore * 0.2) +
                (metaDescriptionScore * 0.15) +
                (keywordDensityScore * 0.25) +
                (readabilityScore * 0.2) +
                (headingStructureScore * 0.2)
        );

        // Build recommendations
        List<String> recs = new ArrayList<>();
        if (contentLengthScore < 100) {
            recs.add("Increase word count: Aim for at least 1,000 words to improve SEO depth.");
        }
        if (metaDescriptionScore < 100) {
            recs.add("Optimize meta description length: Maintain between 150 and 160 characters.");
        }
        if (keywordDensityScore < 100 && targetKeyword != null && !targetKeyword.isBlank()) {
            if (keywordDensity < 1.0) {
                recs.add(String.format("Increase keyword density: Add target keyword '%s' in a few more sections.", targetKeyword));
            } else if (keywordDensity > 3.0) {
                recs.add(String.format("Reduce keyword density: Remove some instances of '%s' to avoid search engine penalties.", targetKeyword));
            }
        }
        if (readabilityScore < 90) {
            recs.add(String.format("Improve readability: Shorten long sentences (average length is %.1f words). Keep them under 20 words.", avgSentenceLength));
        }
        if (!hasH2) {
            recs.add("Add subheadings (H2): Break up your content with H2 tags for better structure.");
        }
        if (!hasH3) {
            recs.add("Add deep subheadings (H3): Use H3 tags inside H2 sections to group sub-topics.");
        }
        if (recs.isEmpty()) {
            recs.add("Excellent! Your content is highly optimized for SEO.");
        }

        String recommendationsText = String.join("\n", recs);

        SeoReport report = SeoReport.builder()
                .blog(blog)
                .overallScore(overallScore)
                .keywordDensityScore(keywordDensityScore)
                .readabilityScore(readabilityScore)
                .headingStructureScore(headingStructureScore)
                .metaDescriptionScore(metaDescriptionScore)
                .contentLengthScore(contentLengthScore)
                .recommendations(recommendationsText)
                .keywordAnalysis(keywordAnalysisText)
                .headingAnalysis(headingAnalysisText)
                .build();

        SeoReport saved = seoReportRepository.save(report);

        // Update blog's SEO score
        blog.setSeoScore(overallScore);
        if (targetKeyword != null && !targetKeyword.isBlank()) {
            blog.setKeywords(targetKeyword);
        }
        blogRepository.save(blog);

        log.info("SEO Analysis complete for blog {}: score={}", blogId, overallScore);
        return SeoReportResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SeoReportResponse getLatestReport(Long blogId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));

        checkBlogAccess(blog, user);

        SeoReport report = seoReportRepository.findFirstByBlogOrderByAnalyzedAtDesc(blog)
                .orElseThrow(() -> new ResourceNotFoundException("SeoReport for blog", blogId));

        return SeoReportResponse.fromEntity(report);
    }

    @Override
    public SeoReportResponse analyzeText(String text, String targetKeyword) {
        String content = text != null ? text : "";
        int totalWords = countWords(content);

        // 1. Content Length Score
        int contentLengthScore = 40;
        if (totalWords >= 1000) {
            contentLengthScore = 100;
        } else if (totalWords >= 500) {
            contentLengthScore = 70;
        }

        // 2. Keyword Density Score
        int keywordCount = 0;
        double keywordDensity = 0.0;
        int keywordDensityScore = 0;
        String keywordAnalysisText = "No target keyword specified.";

        if (targetKeyword != null && !targetKeyword.isBlank()) {
            keywordCount = countKeywordMatches(content, targetKeyword);
            if (totalWords > 0) {
                keywordDensity = ((double) keywordCount / totalWords) * 100;
            }
            if (keywordDensity >= 1.0 && keywordDensity <= 3.0) {
                keywordDensityScore = 100;
            } else if (keywordDensity > 0.0 && keywordDensity < 1.0) {
                keywordDensityScore = (int) (keywordDensity * 100);
            } else if (keywordDensity > 3.0 && keywordDensity <= 5.0) {
                keywordDensityScore = 80;
            } else if (keywordDensity > 5.0) {
                keywordDensityScore = 40;
            }
            keywordAnalysisText = String.format("Target keyword '%s' found %d times (Density: %.2f%%). Ideal density is 1.0%% to 3.0%%.",
                    targetKeyword, keywordCount, keywordDensity);
        }

        // 3. Readability Score
        int readabilityScore = 100;
        int sentenceCount = countSentences(content);
        double avgSentenceLength = 0;
        if (sentenceCount > 0 && totalWords > 0) {
            avgSentenceLength = (double) totalWords / sentenceCount;
            if (avgSentenceLength < 15) {
                readabilityScore = 100;
            } else if (avgSentenceLength < 20) {
                readabilityScore = 90;
            } else if (avgSentenceLength < 25) {
                readabilityScore = 70;
            } else {
                readabilityScore = 40;
            }
        }

        // 4. Heading Structure Score
        boolean hasH1 = content.contains("\n# ") || content.startsWith("# ");
        boolean hasH2 = content.contains("\n## ") || content.contains("## ");
        boolean hasH3 = content.contains("\n### ") || content.contains("### ");

        int headingStructureScore = 20;
        if (hasH1 && hasH2 && hasH3) {
            headingStructureScore = 100;
        } else if (hasH1 && hasH2) {
            headingStructureScore = 80;
        } else if (hasH1) {
            headingStructureScore = 50;
        }

        String headingAnalysisText = String.format("H1 Present: %b, H2 Present: %b, H3 Present: %b.", hasH1, hasH2, hasH3);

        // Overall Score (Weighted Average)
        int overallScore = (int) (
                (contentLengthScore * 0.25) +
                (keywordDensityScore * 0.3) +
                (readabilityScore * 0.25) +
                (headingStructureScore * 0.2)
        );

        // Build recommendations
        List<String> recs = new ArrayList<>();
        if (contentLengthScore < 100) {
            recs.add("Increase word count: Aim for at least 1,000 words to improve SEO depth.");
        }
        if (keywordDensityScore < 100 && targetKeyword != null && !targetKeyword.isBlank()) {
            if (keywordDensity < 1.0) {
                recs.add(String.format("Increase keyword density: Add target keyword '%s' in a few more sections.", targetKeyword));
            } else if (keywordDensity > 3.0) {
                recs.add(String.format("Reduce keyword density: Remove some instances of '%s' to avoid search engine penalties.", targetKeyword));
            }
        }
        if (readabilityScore < 90) {
            recs.add(String.format("Improve readability: Shorten long sentences (average length is %.1f words). Keep them under 20 words.", avgSentenceLength));
        }
        if (!hasH2) {
            recs.add("Add subheadings (H2): Break up your content with H2 tags for better structure.");
        }
        if (!hasH3) {
            recs.add("Add deep subheadings (H3): Use H3 tags inside H2 sections to group sub-topics.");
        }
        if (recs.isEmpty()) {
            recs.add("Excellent! Your content is highly optimized for SEO.");
        }

        String recommendationsText = String.join("\n", recs);

        return SeoReportResponse.builder()
                .overallScore(overallScore)
                .keywordDensityScore(keywordDensityScore)
                .readabilityScore(readabilityScore)
                .headingStructureScore(headingStructureScore)
                .metaDescriptionScore(100)
                .contentLengthScore(contentLengthScore)
                .recommendations(recommendationsText)
                .keywordAnalysis(keywordAnalysisText)
                .headingAnalysis(headingAnalysisText)
                .analyzedAt(java.time.LocalDateTime.now())
                .build();
    }

    // ===== Helpers =====
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void checkBlogAccess(Blog blog, User user) {
        boolean isOwner = blog.getWorkspace().getOwner().getId().equals(user.getId());
        boolean isMember = blog.getWorkspace().getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (!isOwner && !isMember) {
            throw new UnauthorizedException("You do not have access to this blog");
        }
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }

    private int countKeywordMatches(String text, String keyword) {
        if (text == null || text.isBlank() || keyword == null || keyword.isBlank()) return 0;
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword.toLowerCase()) + "\\b");
        Matcher matcher = pattern.matcher(text.toLowerCase());
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int countSentences(String text) {
        if (text == null || text.isBlank()) return 0;
        // Simple sentence boundary detection
        String[] sentences = text.split("[.!?]+");
        return (int) Arrays.stream(sentences).filter(s -> !s.isBlank()).count();
    }
}
