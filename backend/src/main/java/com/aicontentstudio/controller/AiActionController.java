package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.AiInlineActionRequest;
import com.aicontentstudio.dto.request.AiRewriteRequest;
import com.aicontentstudio.dto.response.MessageResponse;
import com.aicontentstudio.service.AiActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Actions", description = "AI text formatting, rewrite, and chat Q&A tools")
public class AiActionController {

    private final AiActionService aiActionService;

    @PostMapping("/rewrite")
    @Operation(summary = "Rewrite text in a specific tone")
    public ResponseEntity<MessageResponse> rewrite(
            @Valid @RequestBody AiRewriteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String result = aiActionService.rewrite(request, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.of(result));
    }

    @PostMapping("/action")
    @Operation(summary = "Perform an inline AI editing action (expand, shorten, fix grammar, improve SEO)")
    public ResponseEntity<MessageResponse> inlineAction(
            @Valid @RequestBody AiInlineActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String result = aiActionService.inlineAction(request, userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.of(result));
    }

    @PostMapping("/ask")
    @Operation(summary = "Ask the AI assistant a question about a specific blog content")
    public ResponseEntity<MessageResponse> askAboutContent(
            @Valid @RequestBody com.aicontentstudio.dto.request.AiAskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String result = aiActionService.askAboutContent(request.getBlogId(), request.getQuestion(), userDetails.getUsername());
        return ResponseEntity.ok(MessageResponse.of(result));
    }

    @PostMapping("/chatbot")
    @Operation(summary = "Universal workspace AI Chatbot to consult or modify assets in-place")
    public ResponseEntity<com.aicontentstudio.dto.response.ChatbotResponse> chat(
            @Valid @RequestBody com.aicontentstudio.dto.request.ChatbotRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(aiActionService.handleUniversalChat(request, userDetails.getUsername()));
    }

    @PostMapping("/ad-copy")
    @Operation(summary = "Generate marketing ad variations based on product descriptions")
    public ResponseEntity<com.aicontentstudio.dto.response.MessageResponse> generateAdCopy(
            @Valid @RequestBody com.aicontentstudio.dto.request.AdCopyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String result = aiActionService.generateAdCopy(request, userDetails.getUsername());
        return ResponseEntity.ok(com.aicontentstudio.dto.response.MessageResponse.of(result));
    }

    @PostMapping("/detect")
    @Operation(summary = "Audit content perplexity to evaluate AI generation likelihood scores")
    public ResponseEntity<com.aicontentstudio.dto.response.AiDetectResponse> detectAiContent(
            @Valid @RequestBody com.aicontentstudio.dto.request.AiDetectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(aiActionService.detectAiContent(request, userDetails.getUsername()));
    }
}
