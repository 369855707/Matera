package com.maternity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maternity.dto.UpdateMatronProfileRequest;
import com.maternity.dto.UpdateMotherProfileRequest;
import com.maternity.dto.UserDTO;
import com.maternity.exception.ResourceNotFoundException;
import com.maternity.model.MatronProfile;
import com.maternity.model.MotherProfile;
import com.maternity.model.User;
import com.maternity.repository.MatronProfileRepository;
import com.maternity.repository.MotherProfileRepository;
import com.maternity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    private final UserRepository userRepository;
    private final MotherProfileRepository motherProfileRepository;
    private final MatronProfileRepository matronProfileRepository;
    private final ObjectMapper objectMapper;

    public UserProfileService(UserRepository userRepository,
                             MotherProfileRepository motherProfileRepository,
                             MatronProfileRepository matronProfileRepository,
                             ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.motherProfileRepository = motherProfileRepository;
        this.matronProfileRepository = matronProfileRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserDTO updateMotherProfile(Long userId, UpdateMotherProfileRequest request) {
        log.info("Updating mother profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != User.UserRole.MOTHER) {
            throw new RuntimeException("User is not a mother");
        }

        // Find or create mother profile
        MotherProfile profile = motherProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    MotherProfile newProfile = new MotherProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        // Update mother-specific fields
        if (request.getDueDate() != null) {
            profile.setDueDate(request.getDueDate());
        }
        if (request.getBabyBirthDate() != null) {
            profile.setBabyBirthDate(request.getBabyBirthDate());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getSpecialNeeds() != null) {
            profile.setSpecialNeeds(request.getSpecialNeeds());
        }

        motherProfileRepository.save(profile);

        // Mark profile as completed
        user.setProfileCompleted(true);
        User savedUser = userRepository.save(user);

        log.info("Mother profile updated successfully for user ID: {}", userId);
        return UserDTO.fromEntity(savedUser);
    }

    @Transactional
    public UserDTO updateMatronProfile(Long userId, UpdateMatronProfileRequest request) {
        log.info("Updating matron profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != User.UserRole.MATRON) {
            throw new RuntimeException("User is not a matron");
        }

        // Find or create matron profile
        MatronProfile profile = matronProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    MatronProfile newProfile = new MatronProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        // Update matron-specific fields
        if (request.getExperienceYears() != null) {
            profile.setYearsOfExperience(request.getExperienceYears());
        }
        if (request.getCertificates() != null && !request.getCertificates().isEmpty()) {
            profile.setCertifications(request.getCertificates());
        }
        if (request.getServiceArea() != null) {
            profile.setLocation(request.getServiceArea());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        matronProfileRepository.save(profile);

        // Mark profile as completed
        user.setProfileCompleted(true);
        User savedUser = userRepository.save(user);

        log.info("Matron profile updated successfully for user ID: {}", userId);
        return UserDTO.fromEntity(savedUser);
    }
}
