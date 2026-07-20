package com.aicontentstudio.service;

import com.aicontentstudio.dto.request.AiInlineActionRequest;
import com.aicontentstudio.dto.request.AiRewriteRequest;

/**
 * AI text-editing actions (rewrite, expand, shorten, fix-grammar, improve-seo).
 */
public interface AiActionService {

    /**
     * Rewrite the supplied text in the requested tone.
     * Logs the AI call and enforces per-user rate limits.
     */
    String rewrite(AiRewriteRequest request, String userEmail);

    /**
     * Apply an inline action (expand | shorten | fix_grammar | improve_seo).
     * Logs the AI call and enforces per-user rate limits.
     */
    String inlineAction(AiInlineActionRequest request, String userEmail);

    /**
     * Answer a question in the context of a given blog article.
     * blogId identifies the article whose content is passed as context.
     */
    String askAboutContent(Long blogId, String question, String userEmail);

    /**
     * Universal chatbot to answer general questions, analyze content, or edit workspace assets in-place.
     */
    com.aicontentstudio.dto.response.ChatbotResponse handleUniversalChat(
            com.aicontentstudio.dto.request.ChatbotRequest request, String userEmail);

    /**
     * Generate marketing ad variants based on product descriptions.
     */
    String generateAdCopy(com.aicontentstudio.dto.request.AdCopyRequest request, String userEmail);

    /**
     * Audit content perplexity to evaluate AI generation likelihood scores.
     */
    com.aicontentstudio.dto.response.AiDetectResponse detectAiContent(
            com.aicontentstudio.dto.request.AiDetectRequest request, String userEmail);
}
