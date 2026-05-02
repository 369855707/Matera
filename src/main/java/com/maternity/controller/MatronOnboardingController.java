package com.maternity.controller;

import com.maternity.dto.MatronOnboardingApplicationRequest;
import com.maternity.dto.MatronOnboardingApplicationResponse;
import com.maternity.dto.MatronOnboardingDocumentResponse;
import com.maternity.model.MatronOnboardingDocumentType;
import com.maternity.service.MatronOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/matron-onboarding")
@Tag(name = "Matron Onboarding", description = "Self-service onboarding for matrons")
@SecurityRequirement(name = "Bearer Authentication")
public class MatronOnboardingController {

    private final MatronOnboardingService matronOnboardingService;

    public MatronOnboardingController(MatronOnboardingService matronOnboardingService) {
        this.matronOnboardingService = matronOnboardingService;
    }

    @PutMapping("/me")
    @Operation(summary = "Save matron onboarding draft", description = "Create or update the current matron's onboarding draft")
    public ResponseEntity<MatronOnboardingApplicationResponse> saveDraft(@Valid @RequestBody MatronOnboardingApplicationRequest request) {
        return ResponseEntity.ok(matronOnboardingService.saveDraftOrUpdate(request));
    }

    @PostMapping("/me/submit")
    @Operation(summary = "Submit matron onboarding application", description = "Submit the current matron's onboarding application for review")
    public ResponseEntity<MatronOnboardingApplicationResponse> submit(@Valid @RequestBody MatronOnboardingApplicationRequest request) {
        return ResponseEntity.ok(matronOnboardingService.submit(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current matron onboarding application", description = "Retrieve the current matron's onboarding application")
    public ResponseEntity<MatronOnboardingApplicationResponse> getCurrentApplication() {
        return ResponseEntity.ok(matronOnboardingService.getCurrentApplication());
    }

    @PostMapping(value = "/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload onboarding document", description = "Upload a profile photo, identity card, or certificate")
    public ResponseEntity<MatronOnboardingDocumentResponse> uploadDocument(
            @RequestParam MatronOnboardingDocumentType documentType,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(matronOnboardingService.uploadDocument(documentType, file));
    }

    @GetMapping("/me/documents/{documentId}")
    @Operation(summary = "Download own onboarding document", description = "Download a document uploaded by the current matron")
    public ResponseEntity<Resource> downloadOwnDocument(@PathVariable Long documentId) {
        return matronOnboardingService.downloadOwnDocument(documentId);
    }
}

