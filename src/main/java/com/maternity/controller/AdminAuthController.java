package com.maternity.controller;

import com.maternity.dto.AdminLoginRequest;
import com.maternity.dto.AuthResponse;
import com.maternity.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "Admin Authentication", description = "Admin authentication endpoints")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    @Operation(summary = "Admin login", description = "Authenticate an admin user and receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AuthResponse response = adminAuthService.login(request);
        return ResponseEntity.ok(response);
    }
}
