package com.maternity.controller;

import com.maternity.service.MatronOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/matron-onboarding-documents")
@Tag(name = "Public Matron Media", description = "Public media for approved matron applications")
public class PublicMatronOnboardingMediaController {

    private final MatronOnboardingService matronOnboardingService;

    public PublicMatronOnboardingMediaController(MatronOnboardingService matronOnboardingService) {
        this.matronOnboardingService = matronOnboardingService;
    }

    @GetMapping("/{documentId}")
    @Operation(summary = "Get public onboarding media", description = "Retrieve a public document for an approved matron application")
    public ResponseEntity<Resource> getPublicDocument(@PathVariable Long documentId) {
        return matronOnboardingService.downloadPublicDocument(documentId);
    }
}

