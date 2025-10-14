package com.maternity.controller;

import com.maternity.dto.UpdateMatronProfileRequest;
import com.maternity.dto.UpdateMotherProfileRequest;
import com.maternity.dto.UserDTO;
import com.maternity.exception.ResourceNotFoundException;
import com.maternity.model.User;
import com.maternity.repository.UserRepository;
import com.maternity.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;

    public UserProfileController(UserProfileService userProfileService, UserRepository userRepository) {
        this.userProfileService = userProfileService;
        this.userRepository = userRepository;
    }

    /**
     * Get the current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication.getName(); // This could be email, phone, or wechatOpenId

        log.info("Getting current user with identifier: {}", identifier);

        // Try to find by email first
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .or(() -> userRepository.findByWechatOpenId(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Operation(summary = "Update Mother Profile",
               description = "Update profile information for a mother user")
    @PutMapping("/profile/mother")
    public ResponseEntity<UserDTO> updateMotherProfile(
            @Valid @RequestBody UpdateMotherProfileRequest request) {
        User currentUser = getCurrentUser();
        log.info("Updating mother profile for user: {}", currentUser.getId());

        UserDTO updatedUser = userProfileService.updateMotherProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Update Matron Profile",
               description = "Update profile information for a matron user")
    @PutMapping("/profile/matron")
    public ResponseEntity<UserDTO> updateMatronProfile(
            @Valid @RequestBody UpdateMatronProfileRequest request) {
        User currentUser = getCurrentUser();
        log.info("Updating matron profile for user: {}", currentUser.getId());

        UserDTO updatedUser = userProfileService.updateMatronProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Get Current User Profile",
               description = "Get the profile of the currently authenticated user")
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        User currentUser = getCurrentUser();
        log.info("Getting profile for user: {}", currentUser.getId());

        return ResponseEntity.ok(UserDTO.fromEntity(currentUser));
    }
}
