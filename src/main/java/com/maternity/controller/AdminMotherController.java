package com.maternity.controller;

import com.maternity.dto.AdminCreateUserRequest;
import com.maternity.dto.AdminUpdateMotherRequest;
import com.maternity.dto.AdminUserDTO;
import com.maternity.service.AdminMotherService;
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
@RequestMapping("/api/admin/mothers")
@Tag(name = "Admin Mother Management", description = "Admin endpoints for managing mothers")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMotherController {

    private final AdminMotherService adminMotherService;

    public AdminMotherController(AdminMotherService adminMotherService) {
        this.adminMotherService = adminMotherService;
    }

    @GetMapping
    @Operation(summary = "Get all mothers", description = "Retrieve all mothers with pagination")
    public ResponseEntity<Page<AdminUserDTO>> getAllMothers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminMotherService.getAllMothers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mother by ID", description = "Retrieve a specific mother by ID")
    public ResponseEntity<AdminUserDTO> getMotherById(@PathVariable Long id) {
        return ResponseEntity.ok(adminMotherService.getMotherById(id));
    }

    @PostMapping
    @Operation(summary = "Create mother", description = "Admin creates a new mother")
    public ResponseEntity<AdminUserDTO> createMother(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.ok(adminMotherService.createMother(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update mother", description = "Update an existing mother")
    public ResponseEntity<AdminUserDTO> updateMother(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateMotherRequest request) {
        return ResponseEntity.ok(adminMotherService.updateMother(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mother", description = "Delete a mother by ID")
    public ResponseEntity<Void> deleteMother(@PathVariable Long id) {
        adminMotherService.deleteMother(id);
        return ResponseEntity.noContent().build();
    }
}
