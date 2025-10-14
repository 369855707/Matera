package com.maternity.service;

import com.maternity.dto.AdminCreateMatronProfileRequest;
import com.maternity.model.MatronProfile;
import com.maternity.model.User;
import com.maternity.repository.MatronProfileRepository;
import com.maternity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMatronProfileService {

    private final MatronProfileRepository matronProfileRepository;
    private final UserRepository userRepository;

    public AdminMatronProfileService(MatronProfileRepository matronProfileRepository,
                                    UserRepository userRepository) {
        this.matronProfileRepository = matronProfileRepository;
        this.userRepository = userRepository;
    }

    public Page<MatronProfile> getAllMatronProfiles(Pageable pageable) {
        return matronProfileRepository.findAll(pageable);
    }

    public MatronProfile getMatronProfileById(Long id) {
        return matronProfileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Matron profile not found with id: " + id));
    }

    @Transactional
    public MatronProfile createMatronProfile(AdminCreateMatronProfileRequest request) {
        // Verify user exists and is a MATRON
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        if (user.getRole() != User.UserRole.MATRON) {
            throw new RuntimeException("User is not a MATRON");
        }

        // Check if profile already exists
        if (matronProfileRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Matron profile already exists for user id: " + request.getUserId());
        }

        MatronProfile profile = new MatronProfile();
        profile.setUser(user);
        profile.setAge(request.getAge());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setPricePerMonth(request.getMonthlyRate() != null ? request.getMonthlyRate().doubleValue() : 0.0);
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());
        profile.setIsAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        return matronProfileRepository.save(profile);
    }

    @Transactional
    public MatronProfile updateMatronProfile(Long id, AdminCreateMatronProfileRequest request) {
        MatronProfile profile = matronProfileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Matron profile not found with id: " + id));

        // Update fields
        if (request.getAge() != null) {
            profile.setAge(request.getAge());
        }
        if (request.getYearsOfExperience() != null) {
            profile.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getMonthlyRate() != null) {
            profile.setPricePerMonth(request.getMonthlyRate().doubleValue());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAvailable() != null) {
            profile.setIsAvailable(request.getAvailable());
        }

        return matronProfileRepository.save(profile);
    }

    @Transactional
    public void deleteMatronProfile(Long id) {
        if (!matronProfileRepository.existsById(id)) {
            throw new RuntimeException("Matron profile not found with id: " + id);
        }
        matronProfileRepository.deleteById(id);
    }
}
