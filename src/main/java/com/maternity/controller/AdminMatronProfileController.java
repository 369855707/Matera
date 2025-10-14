package com.maternity.controller;

import com.maternity.dto.AdminCreateMatronProfileRequest;
import com.maternity.model.MatronProfile;
import com.maternity.service.AdminMatronProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/matron-profiles")
@Tag(name = "Admin Matron Profile Management", description = "Admin endpoints for managing matron profiles")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMatronProfileController {

    private final AdminMatronProfileService adminMatronProfileService;

    public AdminMatronProfileController(AdminMatronProfileService adminMatronProfileService) {
        this.adminMatronProfileService = adminMatronProfileService;
    }

    @GetMapping
    @Operation(summary = "Get all matron profiles", description = "Retrieve all matron profiles with pagination")
    public ResponseEntity<Page<MatronProfile>> getAllMatronProfiles(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminMatronProfileService.getAllMatronProfiles(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get matron profile by ID", description = "Retrieve a specific matron profile by ID")
    public ResponseEntity<MatronProfile> getMatronProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(adminMatronProfileService.getMatronProfileById(id));
    }

    @PostMapping
    @Operation(summary = "Create matron profile", description = "Admin creates a matron profile for a user")
    public ResponseEntity<MatronProfile> createMatronProfile(@Valid @RequestBody AdminCreateMatronProfileRequest request) {
        return ResponseEntity.ok(adminMatronProfileService.createMatronProfile(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update matron profile", description = "Update an existing matron profile")
    public ResponseEntity<MatronProfile> updateMatronProfile(
            @PathVariable Long id,
            @Valid @RequestBody AdminCreateMatronProfileRequest request) {
        return ResponseEntity.ok(adminMatronProfileService.updateMatronProfile(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete matron profile", description = "Delete a matron profile by ID")
    public ResponseEntity<Void> deleteMatronProfile(@PathVariable Long id) {
        adminMatronProfileService.deleteMatronProfile(id);
        return ResponseEntity.noContent().build();
    }
}
