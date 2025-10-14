package com.maternity.service;

import com.maternity.model.MotherProfile;
import com.maternity.model.User;
import com.maternity.repository.MotherProfileRepository;
import com.maternity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MotherProfileService {

    private final MotherProfileRepository motherProfileRepository;
    private final UserRepository userRepository;

    public MotherProfileService(MotherProfileRepository motherProfileRepository,
                               UserRepository userRepository) {
        this.motherProfileRepository = motherProfileRepository;
        this.userRepository = userRepository;
    }

    public List<MotherProfile> getAllMotherProfiles() {
        return motherProfileRepository.findAll();
    }

    public Optional<MotherProfile> getMotherProfileById(Long id) {
        return motherProfileRepository.findById(id);
    }

    public Optional<MotherProfile> getMotherProfileByUserId(Long userId) {
        return motherProfileRepository.findByUserId(userId);
    }

    public List<MotherProfile> getExpectingMothers() {
        return motherProfileRepository.findExpectingMothers(LocalDate.now());
    }

    public List<MotherProfile> getMothersWithBabies() {
        return motherProfileRepository.findMothersWithBabies();
    }

    @Transactional
    public MotherProfile createMotherProfile(Long userId, LocalDate dueDate, LocalDate babyBirthDate,
                                            String address, String specialNeeds) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getRole() != User.UserRole.MOTHER) {
            throw new RuntimeException("User is not a MOTHER");
        }

        // Check if profile already exists
        if (motherProfileRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Mother profile already exists for user id: " + userId);
        }

        MotherProfile profile = new MotherProfile();
        profile.setUser(user);
        profile.setDueDate(dueDate);
        profile.setBabyBirthDate(babyBirthDate);
        profile.setAddress(address);
        profile.setSpecialNeeds(specialNeeds);

        return motherProfileRepository.save(profile);
    }

    @Transactional
    public MotherProfile updateMotherProfile(Long profileId, LocalDate dueDate, LocalDate babyBirthDate,
                                            String address, String specialNeeds) {
        MotherProfile profile = motherProfileRepository.findById(profileId)
            .orElseThrow(() -> new RuntimeException("Mother profile not found with id: " + profileId));

        if (dueDate != null) {
            profile.setDueDate(dueDate);
        }
        if (babyBirthDate != null) {
            profile.setBabyBirthDate(babyBirthDate);
        }
        if (address != null) {
            profile.setAddress(address);
        }
        if (specialNeeds != null) {
            profile.setSpecialNeeds(specialNeeds);
        }

        return motherProfileRepository.save(profile);
    }

    @Transactional
    public void deleteMotherProfile(Long profileId) {
        if (!motherProfileRepository.existsById(profileId)) {
            throw new RuntimeException("Mother profile not found with id: " + profileId);
        }
        motherProfileRepository.deleteById(profileId);
    }
}
