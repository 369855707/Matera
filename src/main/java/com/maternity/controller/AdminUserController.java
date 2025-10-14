package com.maternity.controller;

import com.maternity.dto.AdminCreateMatronProfileRequest;
import com.maternity.dto.AdminCreateUserRequest;
import com.maternity.dto.AdminUpdateUserRequest;
import com.maternity.dto.AdminUserDTO;
import com.maternity.dto.CreateMatronRequest;
import com.maternity.model.MatronProfile;
import com.maternity.model.User;
import com.maternity.service.AdminUserService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User Management", description = "Admin endpoints for managing users")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination, optionally filtered by role")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @RequestParam(required = false) User.UserRole role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getAllUsersPaginated(role, pageable));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUserById(userId));
    }

    @GetMapping("/mothers")
    @Operation(summary = "Get all mothers", description = "Retrieve all users with MOTHER role")
    public ResponseEntity<List<AdminUserDTO>> getMothers() {
        return ResponseEntity.ok(adminUserService.getMothers());
    }

    @GetMapping("/matrons")
    @Operation(summary = "Get all matrons", description = "Retrieve all users with MATRON role")
    public ResponseEntity<List<AdminUserDTO>> getMatrons() {
        return ResponseEntity.ok(adminUserService.getMatrons());
    }

    @GetMapping("/search/name")
    @Operation(summary = "Search users by name", description = "Search users by name (case insensitive)")
    public ResponseEntity<List<AdminUserDTO>> searchUsersByName(@RequestParam String name) {
        return ResponseEntity.ok(adminUserService.searchUsersByName(name));
    }

    @GetMapping("/search/phone")
    @Operation(summary = "Search users by phone", description = "Search users by phone number")
    public ResponseEntity<List<AdminUserDTO>> searchUsersByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(adminUserService.searchUsersByPhone(phone));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete a user by ID")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Get statistics about users")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = Map.of(
            "totalUsers", adminUserService.getTotalUsersCount(),
            "totalMothers", adminUserService.getMothersCount(),
            "totalMatrons", adminUserService.getMatronsCount()
        );
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/matron-profiles")
    @Operation(summary = "Get all matron profiles", description = "Retrieve all matron profiles")
    public ResponseEntity<List<MatronProfile>> getAllMatronProfiles() {
        return ResponseEntity.ok(adminUserService.getAllMatronProfiles());
    }

    @GetMapping("/matron-profiles/{profileId}")
    @Operation(summary = "Get matron profile by ID", description = "Retrieve a specific matron profile by ID")
    public ResponseEntity<MatronProfile> getMatronProfileById(@PathVariable Long profileId) {
        return ResponseEntity.ok(adminUserService.getMatronProfileById(profileId));
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Admin creates a new user (no password required)")
    public ResponseEntity<AdminUserDTO> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.ok(adminUserService.createUser(request));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Admin updates an existing user")
    public ResponseEntity<AdminUserDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(userId, request));
    }

    @PostMapping("/matron-profiles")
    @Operation(summary = "Create matron profile", description = "Admin creates a matron profile for a user")
    public ResponseEntity<MatronProfile> createMatronProfile(@Valid @RequestBody AdminCreateMatronProfileRequest request) {
        return ResponseEntity.ok(adminUserService.createMatronProfile(request));
    }

    @PostMapping("/matrons")
    @Operation(summary = "Create matron", description = "Admin creates a complete matron (user + profile)")
    public ResponseEntity<MatronProfile> createMatron(@Valid @RequestBody CreateMatronRequest request) {
        return ResponseEntity.ok(adminUserService.createMatron(request));
    }
}
