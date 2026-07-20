package com.aicontentstudio.service;

import com.aicontentstudio.enums.AiTone;

/**
 * Abstraction over AI providers. Implementations: GroqAiContentServiceImpl, OllamaAiContentServiceImpl.
 * Switch via app.ai.provider = groq | ollama
 */
public interface AiContentService {

    /**
     * Generate a full SEO blog article.
     */
    String generateBlog(String topic, String audience, AiTone tone, String keywords, int targetWordCount);

    /**
     * Rewrite text in the specified tone.
     */
    String rewriteText(String text, AiTone tone);

    /**
     * Expand a short text into a longer version.
     */
    String expandText(String text);

    /**
     * Shorten text while keeping key ideas.
     */
    String shortenText(String text);

    /**
     * Fix grammar and improve clarity.
     */
    String fixGrammar(String text);

    /**
     * Improve SEO: add keywords naturally, improve heading suggestions.
     */
    String improveSeo(String text, String keywords);

    /**
     * Generate multiple headline variants (SEO, clickbait, LinkedIn, professional).
     * Returns JSON string of variants.
     */
    String generateHeadlines(String topic, String content);

    /**
     * Generate social media post for a specific platform.
     */
    String generateSocialPost(String platform, String blogContent, String blogTitle);

    /**
     * Generate an email campaign.
     */
    String generateEmail(String emailType, String subject, String context, String audience);

    /**
     * Context-aware Q&A: answer a question about the given article content.
     */
    String askAboutContent(String question, String articleContent);

    /**
     * Universal chatbot request to analyze or edit content.
     * Returns a JSON string with keys "reply" and "updatedContent".
     */
    String askChatbot(String message, String content, String contextType);

    /**
     * Return the name of the current provider (for audit logs).
     */
    String getProviderName();

    /**
     * Return the model name being used.
     */
    String getModelName();
}
