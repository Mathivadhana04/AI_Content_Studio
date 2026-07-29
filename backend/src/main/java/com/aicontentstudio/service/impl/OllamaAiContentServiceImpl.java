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
 * Ollama AI provider — uses local Ollama server (http://localhost:11434).
 * Uses OpenAI-compatible endpoint available in Ollama >= 0.1.24.
 * Activated when app.ai.provider=ollama
 */
@Service
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama")
@Slf4j
public class OllamaAiContentServiceImpl implements AiContentService {

    @Value("${app.ollama.base-url}")
    private String baseUrl;

    @Value("${app.ollama.model}")
    private String model;

    @Value("${app.ollama.max-tokens}")
    private int maxTokens;

    @Value("${app.ollama.temperature}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OllamaAiContentServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // Uses Ollama's OpenAI-compatible endpoint
    private String complete(String systemPrompt, String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
                ),
                "stream", false,
                "options", Map.of(
                    "num_predict", maxTokens,
                    "temperature", temperature
                )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            // Ollama's OpenAI-compatible endpoint
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            log.error("Ollama API call failed: {}", e.getMessage(), e);
            throw new ApiException("Ollama service unavailable. Make sure Ollama is running on " + baseUrl,
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public String generateBlog(String topic, String audience, AiTone tone, String keywords, int targetWordCount) {
        String systemPrompt = "You are an expert SEO content writer. Rules: " +
            "1. The H1 title MUST be short, minimal, keyword-focused — maximum 4 words. NOT a full sentence. Example: 'Youth Stress Guide' or 'Managing Stress Today'. " +
            "2. The meta description (150-160 chars) must be placed as an HTML comment: <!-- META: your description --> and NOT shown as visible text. " +
            "3. Do NOT include any FAQ or Questions section. " +
            "4. Use HTML inline-styled headings: H1=<h1 style=\"color: #a78bfa; border-bottom: 2px solid #8b5cf6; padding-bottom: 8px;\">, " +
            "H2=<h2 style=\"color: #ec4899; border-left: 4px solid #8b5cf6; padding-left: 8px; margin-top: 24px;\">, " +
            "H3=<h3 style=\"color: #10b981; margin-top: 16px;\">.";
        String userPrompt = String.format(
            "Write a %d-word blog post about: %s\nAudience: %s\nTone: %s\nKeywords: %s\n\n" +
            "Structure:\n<h1 style=\"color: #a78bfa;\">[2-4 word keyword title — not a sentence]</h1>\n" +
            "<!-- META: [150-160 char meta description] -->\n" +
            "<h2>Introduction</h2>[intro]\n[3-5 H2 sections with H3 sub-sections]\n<h2>Conclusion</h2>[summary]. No FAQ section.",
            targetWordCount, topic, audience, tone.name().toLowerCase(), keywords);
        return complete(systemPrompt, userPrompt);
    }

    @Override
    public String rewriteText(String text, AiTone tone) {
        return complete(
            "You are a professional editor. Rewrite text in the requested tone. Return ONLY the rewritten text, with no introductory or concluding statements, notes, or markdown code blocks.",
            String.format("Rewrite in %s tone:\n\n%s", tone.name().toLowerCase(), text));
    }

    @Override
    public String expandText(String text) {
        return complete("You are a content expander. Return ONLY the expanded text itself, with no comments, explanations, or wrapping notes.", "Expand this text with more detail:\n\n" + text);
    }

    @Override
    public String shortenText(String text) {
        return complete("You are a concise editor. Return ONLY the shortened text, with no explanations, conversational remarks, or metadata.", "Shorten to half length keeping key points:\n\n" + text);
    }

    @Override
    public String fixGrammar(String text) {
        return complete("You are a grammar editor. Return ONLY the corrected text. Do NOT include any intro, outro, or notes.", "Fix grammar:\n\n" + text);
    }

    @Override
    public String improveSeo(String text, String keywords) {
        return complete("You are an SEO specialist. Return ONLY the improved content, with no introductory or concluding remarks, explanations, or keyword list summaries.",
            String.format("Improve SEO with keywords: %s\n\nContent:\n%s", keywords, text));
    }

    @Override
    public String generateHeadlines(String topic, String content) {
        return complete("You are a headline writer. Return JSON only.",
            String.format("Generate 4 headlines for '%s'. Return JSON: {\"seo\":\"\",\"professional\":\"\",\"clickbait\":\"\",\"linkedin\":\"\"}", topic));
    }

    @Override
    public String generateSocialPost(String platform, String blogContent, String blogTitle) {
        return complete("You are a social media expert.",
            String.format("Create a %s post for: %s\nContent: %s", platform, blogTitle,
                blogContent.substring(0, Math.min(blogContent.length(), 800))));
    }

    @Override
    public String generateEmail(String emailType, String subject, String context, String audience) {
        return complete("You are an email marketing specialist. Write HTML emails.",
            String.format("Write a %s email. Subject: %s. Audience: %s. Context: %s", emailType, subject, audience, context));
    }

    @Override
    public String askAboutContent(String question, String articleContent) {
        return complete("You are a helpful AI assistant. Answer based on the provided content only.",
            String.format("Content:\n%s\n\nQuestion: %s", articleContent, question));
    }

    @Override
    public String askChatbot(String message, String content, String contextType) {
        String systemPrompt = "You are a universal AI assistant, humanizer, and plagiarism-removal editor. You have access to content of type " + contextType + ". If the user asks to rewrite, humanize, remove plagiarism, or edit text (provided in context or user message), you MUST rewrite the text completely and return the full rewritten text inside the 'updatedContent' field of the JSON. Return a JSON response with keys 'reply' and 'updatedContent'. Do NOT return any explanations outside of the JSON block.";
        String userPrompt = String.format("Context Asset:\n%s\n\nUser Message / Text to Rewrite: %s", content != null ? content : "(no asset selected)", message);
        return complete(systemPrompt, userPrompt);
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public String getModelName() {
        return model;
    }
}
