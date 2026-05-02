package com.maternity.controller;

import com.maternity.dto.MatronOnboardingApplicationResponse;
import com.maternity.dto.RejectMatronOnboardingApplicationRequest;
import com.maternity.model.MatronOnboardingApplicationStatus;
import com.maternity.service.MatronOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/matron-onboarding-applications")
@Tag(name = "Admin Matron Onboarding", description = "Admin endpoints for matron onboarding review")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMatronOnboardingController {

    private final MatronOnboardingService matronOnboardingService;

    public AdminMatronOnboardingController(MatronOnboardingService matronOnboardingService) {
        this.matronOnboardingService = matronOnboardingService;
    }

    @GetMapping
    @Operation(summary = "List onboarding applications", description = "List onboarding applications with optional status filter")
    public ResponseEntity<Page<MatronOnboardingApplicationResponse>> listApplications(
            @RequestParam(required = false) MatronOnboardingApplicationStatus status,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(matronOnboardingService.listApplications(status, pageable));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get onboarding application", description = "Get a single onboarding application")
    public ResponseEntity<MatronOnboardingApplicationResponse> getApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(matronOnboardingService.getApplicationById(applicationId));
    }

    @PostMapping("/{applicationId}/approve")
    @Operation(summary = "Approve onboarding application", description = "Approve the application and activate the matron")
    public ResponseEntity<MatronOnboardingApplicationResponse> approve(@PathVariable Long applicationId) {
        return ResponseEntity.ok(matronOnboardingService.approve(applicationId));
    }

    @PostMapping("/{applicationId}/reject")
    @Operation(summary = "Reject onboarding application", description = "Reject the application with review notes")
    public ResponseEntity<MatronOnboardingApplicationResponse> reject(
            @PathVariable Long applicationId,
            @Valid @RequestBody RejectMatronOnboardingApplicationRequest request) {
        return ResponseEntity.ok(matronOnboardingService.reject(applicationId, request));
    }

    @GetMapping("/{applicationId}/documents/{documentId}")
    @Operation(summary = "Download application document", description = "Download a document for admin review")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long applicationId, @PathVariable Long documentId) {
        return matronOnboardingService.downloadAdminDocument(applicationId, documentId);
    }
}
