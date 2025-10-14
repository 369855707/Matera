package com.maternity.service;

import com.maternity.dto.AdminCreateUserRequest;
import com.maternity.dto.AdminUpdateMotherRequest;
import com.maternity.dto.AdminUserDTO;
import com.maternity.model.MotherProfile;
import com.maternity.model.User;
import com.maternity.repository.MotherProfileRepository;
import com.maternity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMotherService {

    private final UserRepository userRepository;
    private final MotherProfileRepository motherProfileRepository;

    public AdminMotherService(UserRepository userRepository,
                             MotherProfileRepository motherProfileRepository) {
        this.userRepository = userRepository;
        this.motherProfileRepository = motherProfileRepository;
    }

    public Page<AdminUserDTO> getAllMothers(Pageable pageable) {
        return userRepository.findByRole(User.UserRole.MOTHER, pageable)
            .map(this::convertToDTO);
    }

    public AdminUserDTO getMotherById(Long id) {
        User mother = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mother not found with id: " + id));
        if (mother.getRole() != User.UserRole.MOTHER) {
            throw new RuntimeException("User is not a MOTHER");
        }
        return convertToDTO(mother);
    }

    private AdminUserDTO convertToDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO(user);

        // Load mother profile data if user is a MOTHER
        if (user.getRole() == User.UserRole.MOTHER) {
            motherProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                dto.setDueDate(profile.getDueDate() != null ? profile.getDueDate().atStartOfDay() : null);
                dto.setBabyBirthDate(profile.getBabyBirthDate() != null ? profile.getBabyBirthDate().atStartOfDay() : null);
                dto.setAddress(profile.getAddress());
                dto.setSpecialNeeds(profile.getSpecialNeeds());
            });
        }

        return dto;
    }

    @Transactional
    public AdminUserDTO createMother(AdminCreateUserRequest request) {
        // Check if email already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Check if phone already exists
        if (request.getPhone() != null && userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone already exists: " + request.getPhone());
        }

        User mother = new User();
        mother.setName(request.getName());
        mother.setEmail(request.getEmail());
        mother.setPhone(request.getPhone());
        mother.setRole(User.UserRole.MOTHER);
        mother.setPassword(null);
        mother.setProfileCompleted(false);

        User savedMother = userRepository.save(mother);

        // Create mother profile
        MotherProfile motherProfile = new MotherProfile();
        motherProfile.setUser(savedMother);
        motherProfile.setDueDate(request.getDueDate());
        motherProfile.setBabyBirthDate(request.getBabyBirthDate());
        motherProfile.setAddress(request.getAddress());
        motherProfile.setSpecialNeeds(request.getSpecialNeeds());
        motherProfileRepository.save(motherProfile);

        return convertToDTO(savedMother);
    }

    @Transactional
    public AdminUserDTO updateMother(Long id, AdminUpdateMotherRequest request) {
        User mother = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mother not found with id: " + id));

        if (mother.getRole() != User.UserRole.MOTHER) {
            throw new RuntimeException("User is not a MOTHER");
        }

        // Update fields
        if (request.getName() != null) {
            mother.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(mother.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            mother.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().equals(mother.getPhone())) {
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new RuntimeException("Phone already exists: " + request.getPhone());
            }
            mother.setPhone(request.getPhone());
        }

        User updatedMother = userRepository.save(mother);

        // Update mother profile
        MotherProfile motherProfile = motherProfileRepository.findByUserId(id)
            .orElseGet(() -> {
                MotherProfile newProfile = new MotherProfile();
                newProfile.setUser(mother);
                return newProfile;
            });

        if (request.getAddress() != null) {
            motherProfile.setAddress(request.getAddress());
        }
        if (request.getDueDate() != null) {
            motherProfile.setDueDate(request.getDueDate());
        }
        if (request.getBabyBirthDate() != null) {
            motherProfile.setBabyBirthDate(request.getBabyBirthDate());
        }
        if (request.getSpecialNeeds() != null) {
            motherProfile.setSpecialNeeds(request.getSpecialNeeds());
        }

        motherProfileRepository.save(motherProfile);

        return convertToDTO(updatedMother);
    }

    @Transactional
    public void deleteMother(Long id) {
        User mother = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mother not found with id: " + id));

        if (mother.getRole() != User.UserRole.MOTHER) {
            throw new RuntimeException("User is not a MOTHER");
        }

        userRepository.deleteById(id);
    }
}
