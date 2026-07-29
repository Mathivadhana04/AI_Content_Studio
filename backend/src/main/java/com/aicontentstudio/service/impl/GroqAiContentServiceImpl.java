package com.aicontentstudio.service.impl;

import com.aicontentstudio.enums.AiTone;
import com.aicontentstudio.exception.ApiException;
import com.aicontentstudio.service.AiContentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Groq AI provider — uses OpenAI-compatible chat completions API.
 * Free tier supports Llama 3.3, Mixtral, Gemma models.
 * Activated when app.ai.provider=groq
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "groq")
@Slf4j
public class GroqAiContentServiceImpl implements AiContentService {

    @Value("${app.groq.api-key}")
    private String apiKey;

    @Value("${app.groq.base-url}")
    private String baseUrl;

    @Value("${app.groq.model}")
    private String model;

    @Value("${app.groq.max-tokens}")
    private int maxTokens;

    @Value("${app.groq.temperature}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GroqAiContentServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ===== Core completion call =====
    private String complete(String systemPrompt, String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
                ),
                "max_tokens", maxTokens,
                "temperature", temperature
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage(), e);
            throw new ApiException("AI service temporarily unavailable: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public String generateBlog(String topic, String audience, AiTone tone, String keywords, int targetWordCount) {
        String systemPrompt = """
            You are an expert SEO content writer outputting blog content as HTML.
            CRITICAL RULES — you MUST follow ALL of these:
            1. NEVER use Markdown. Do NOT use # or ## or ### for headings. Do NOT use **text** for bold meta.
            2. ALL headings MUST be written as HTML tags with inline styles, exactly as shown:
               H1: <h1 style="color: #a78bfa; border-bottom: 2px solid #8b5cf6; padding-bottom: 8px;">3-4 Word Title</h1>
               H2: <h2 style="color: #ec4899; border-left: 4px solid #8b5cf6; padding-left: 8px; margin-top: 24px;">Section Heading</h2>
               H3: <h3 style="color: #10b981; margin-top: 16px;">Sub-heading</h3>
            3. The H1 title must be SHORT — maximum 4 words, keyword-focused. NOT a sentence.
               CORRECT: Youth Stress Guide | WRONG: Understanding and Managing Stress in Youth
            4. The meta description MUST be placed as: <!-- META: your 150-160 char description -->
               It must NOT appear as visible text like **Meta Description:** anywhere.
            5. Do NOT include any FAQ or Frequently Asked Questions section.
            6. Write body paragraphs as plain text (no Markdown symbols).
            """;

        String userPrompt = String.format("""
            Write a %s-word blog post about: %s
            Target audience: %s | Tone: %s | Keywords: %s
            
            Output ONLY HTML. No Markdown whatsoever. Follow this exact structure:
            
            <h1 style="color: #a78bfa; border-bottom: 2px solid #8b5cf6; padding-bottom: 8px;">Youth Stress Guide</h1>
            <!-- META: 150-160 character meta description about the topic -->
            
            <h2 style="color: #ec4899; border-left: 4px solid #8b5cf6; padding-left: 8px; margin-top: 24px;">Introduction</h2>
            <p>Introductory paragraph here...</p>
            
            <h2 style="color: #ec4899; border-left: 4px solid #8b5cf6; padding-left: 8px; margin-top: 24px;">Section Title Here</h2>
            <p>Content paragraph...</p>
            <h3 style="color: #10b981; margin-top: 16px;">Sub-topic if needed</h3>
            <p>More content...</p>
            
            [Continue with 3-5 more H2 sections following the same HTML format]
            
            <h2 style="color: #ec4899; border-left: 4px solid #8b5cf6; padding-left: 8px; margin-top: 24px;">Conclusion</h2>
            <p>Summary and call to action...</p>
            
            REMINDER: Replace the H1 text with a real 3-4 word keyword title specific to the topic. Never use # symbols.
            """, targetWordCount, topic, audience, tone.name().toLowerCase(), keywords);

        return complete(systemPrompt, userPrompt);
    }

    @Override
    public String rewriteText(String text, AiTone tone) {
        String toneDesc = getToneDescription(tone);
        return complete(
            "You are a professional editor. Rewrite the provided text in the specified tone while preserving all key information and meaning. You must return ONLY the rewritten text, with no introductory or concluding statements, notes, or markdown code blocks.",
            String.format("Rewrite the following text in a %s tone:\n\n%s", toneDesc, text)
        );
    }

    @Override
    public String expandText(String text) {
        return complete(
            "You are a content writer. Expand the given text to be more detailed and informative. You must return ONLY the expanded text itself, with no comments, explanations, or wrapping notes.",
            "Expand and enrich the following text with more detail, examples, and depth:\n\n" + text
        );
    }

    @Override
    public String shortenText(String text) {
        return complete(
            "You are an editor specializing in concise writing. Shorten text while preserving key points. You must return ONLY the shortened text, with no explanations, conversational remarks, or metadata.",
            "Shorten the following text to roughly half its length while keeping all essential points:\n\n" + text
        );
    }

    @Override
    public String fixGrammar(String text) {
        return complete(
            "You are a grammar and style editor. Fix all grammar, punctuation, spelling, and clarity issues. Return ONLY the corrected text. Do NOT include any intro, outro, or notes.",
            "Fix grammar, punctuation, and improve clarity of this text:\n\n" + text
        );
    }

    @Override
    public String improveSeo(String text, String keywords) {
        return complete(
            "You are an SEO specialist. Improve content for search engine optimization naturally. You must return ONLY the improved content, with no introductory or concluding remarks, explanations, or keyword list summaries.",
            String.format("Improve the SEO of this content. Naturally incorporate these keywords: %s\n\nContent:\n%s", keywords, text)
        );
    }

    @Override
    public String generateHeadlines(String topic, String content) {
        String response = complete(
            "You are a headline copywriter. Generate compelling headlines in different styles. Return valid JSON.",
            String.format("""
                Generate 4 headline variants for this topic: %s
                Content preview: %s
                
                Return ONLY valid JSON in this exact format:
                {
                  "seo": "SEO-optimized headline with primary keyword",
                  "professional": "Professional, credibility-focused headline",
                  "clickbait": "High curiosity, emotional clickbait headline",
                  "linkedin": "LinkedIn-optimized thought leadership headline"
                }
                """, topic, content.substring(0, Math.min(content.length(), 500)))
        );
        return response;
    }

    @Override
    public String generateSocialPost(String platform, String blogContent, String blogTitle) {
        Map<String, String> platformGuidelines = Map.of(
            "LINKEDIN", "Professional tone, 1300 char max, include 3-5 hashtags, add a CTA",
            "TWITTER", "Punchy, 280 char max, include 2-3 hashtags, use emojis",
            "FACEBOOK", "Conversational, 500 char max, include link preview text",
            "INSTAGRAM", "Visual description + caption, 150 char caption, 10-15 hashtags"
        );

        String guideline = platformGuidelines.getOrDefault(platform.toUpperCase(), "Social media optimized post");

        return complete(
            "You are a social media content strategist. Create platform-optimized social posts.",
            String.format("Create a %s post for this blog article.\nGuidelines: %s\nBlog title: %s\nBlog content: %s",
                platform, guideline, blogTitle, blogContent.substring(0, Math.min(blogContent.length(), 1000)))
        );
    }

    @Override
    public String generateEmail(String emailType, String subject, String context, String audience) {
        return complete(
            "You are an email marketing specialist. Write high-converting, engaging email campaigns in HTML format.",
            String.format("""
                Write a %s email campaign.
                Subject line: %s
                Target audience: %s
                Context/Content: %s
                
                Return the email as HTML with inline styles suitable for email clients.
                Include a compelling subject line variation suggestion at the top as a comment.
                """, emailType, subject, audience, context)
        );
    }

    @Override
    public String askAboutContent(String question, String articleContent) {
        return complete(
            "You are a helpful AI assistant with expertise in the provided article content. Answer questions accurately and concisely based only on the content provided.",
            String.format("Article content:\n%s\n\nQuestion: %s", articleContent, question)
        );
    }

    @Override
    public String askChatbot(String message, String content, String contextType) {
        String systemPrompt = """
            You are a universal AI assistant, content editor, humanizer, and plagiarism-removal consultant.
            Context Type: %s.
            
            CRITICAL REWRITE RULES:
            1. If the user asks you to rewrite, humanize, remove plagiarism, edit, rephrase, expand, or modify text (whether provided in the context asset or directly inside their user prompt), you MUST rewrite the text completely into a high-quality, human-written, plagiarism-free version.
            2. You MUST place the full rewritten text inside the 'updatedContent' property of the JSON. Do NOT set 'updatedContent' to null or leave it empty if a rewrite request was made!
            3. Put a brief explanation in the 'reply' property.
            
            You MUST return a JSON response in this exact format:
            {
              "reply": "Your brief conversational note (e.g., 'Here is the humanized, plagiarism-free version of your text:')",
              "updatedContent": "FULL_REWRITTEN_TEXT_HERE"
            }
            Do NOT include any text or markdown block wrapping outside the JSON. Return ONLY valid raw JSON.
            """.formatted(contextType);

        String userPrompt = """
            Current Asset Context:
            ---
            %s
            ---
            
            User Message / Text to Rewrite:
            %s
            """.formatted(content != null ? content : "(No asset selected in dropdown)", message);

        return complete(systemPrompt, userPrompt);
    }

    @Override
    public String getProviderName() {
        return "groq";
    }

    @Override
    public String getModelName() {
        return model;
    }

    private String getToneDescription(AiTone tone) {
        return switch (tone) {
            case PROFESSIONAL -> "professional and polished";
            case FORMAL -> "formal and structured";
            case ACADEMIC -> "academic and research-oriented";
            case CASUAL -> "casual and conversational";
            case MARKETING -> "persuasive marketing";
            case TECHNICAL -> "technical and precise";
            case HUMANIZED -> "warm, human, and empathetic";
            case SIMPLIFIED -> "simple and easy-to-understand";
            case PERSUASIVE -> "persuasive and compelling";
            case CREATIVE -> "creative and engaging";
        };
    }
}
