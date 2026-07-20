package com.aicontentstudio.controller;

import com.aicontentstudio.dto.request.EmailContentRequest;
import com.aicontentstudio.entity.EmailCampaign;
import com.aicontentstudio.service.EmailContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Tag(name = "Email Content", description = "AI email newsletter and marketing campaign generators")
public class EmailContentController {

    private final EmailContentService emailContentService;

    @PostMapping("/generate")
    @Operation(summary = "Generate an email campaign based on blog content")
    public ResponseEntity<com.aicontentstudio.dto.response.EmailCampaignResponse> generateEmail(
            @Valid @RequestBody EmailContentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.aicontentstudio.dto.response.EmailCampaignResponse.fromEntity(
                        emailContentService.generateEmail(request, userDetails.getUsername())));
    }

    @GetMapping("/my")
    @Operation(summary = "Get user's generated email campaigns (paginated)")
    public ResponseEntity<Page<com.aicontentstudio.dto.response.EmailCampaignResponse>> getUserCampaigns(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(emailContentService.getUserCampaigns(userDetails.getUsername(), pageable)
                .map(com.aicontentstudio.dto.response.EmailCampaignResponse::fromEntity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an email campaign")
    public ResponseEntity<Void> deleteCampaign(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        emailContentService.deleteCampaign(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
