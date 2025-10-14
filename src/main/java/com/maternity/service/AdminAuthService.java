package com.maternity.service;

import com.maternity.dto.AdminDTO;
import com.maternity.dto.AdminLoginRequest;
import com.maternity.dto.AuthResponse;
import com.maternity.model.Admin;
import com.maternity.repository.AdminRepository;
import com.maternity.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AdminAuthService(AdminRepository adminRepository,
                           JwtTokenProvider jwtTokenProvider,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager) {
        this.adminRepository = adminRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse login(AdminLoginRequest request) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                "admin:" + request.getUsername(),
                request.getPassword()
            )
        );

        // Find admin and update last login time
        Admin admin = adminRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getEnabled()) {
            throw new RuntimeException("Admin account is disabled");
        }

        admin.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(admin);

        // Generate token with admin prefix
        String token = jwtTokenProvider.generateToken("admin:" + admin.getUsername());

        return new AuthResponse(token, new AdminDTO(admin), "ADMIN");
    }

    @Transactional
    public Admin createAdmin(String username, String password, String email, String name, Admin.AdminRole role) {
        if (adminRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (email != null && adminRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);
        admin.setName(name);
        admin.setRole(role);
        admin.setEnabled(true);

        return adminRepository.save(admin);
    }

    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
    }
}
