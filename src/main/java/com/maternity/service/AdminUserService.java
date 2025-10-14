package com.maternity.service;

import com.maternity.dto.AdminCreateMatronProfileRequest;
import com.maternity.dto.AdminCreateUserRequest;
import com.maternity.dto.AdminUpdateUserRequest;
import com.maternity.dto.AdminUserDTO;
import com.maternity.dto.CreateMatronRequest;
import com.maternity.model.MatronProfile;
import com.maternity.model.MotherProfile;
import com.maternity.model.User;
import com.maternity.repository.MatronProfileRepository;
import com.maternity.repository.MotherProfileRepository;
import com.maternity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final MatronProfileRepository matronProfileRepository;
    private final MotherProfileRepository motherProfileRepository;

    public AdminUserService(UserRepository userRepository,
                           MatronProfileRepository matronProfileRepository,
                           MotherProfileRepository motherProfileRepository) {
        this.userRepository = userRepository;
        this.matronProfileRepository = matronProfileRepository;
        this.motherProfileRepository = motherProfileRepository;
    }

    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public Page<AdminUserDTO> getAllUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(this::convertToDTO);
    }

    public Page<AdminUserDTO> getAllUsersPaginated(User.UserRole role, Pageable pageable) {
        if (role == null) {
            return getAllUsersPaginated(pageable);
        }
        return userRepository.findByRole(role, pageable)
            .map(this::convertToDTO);
    }

    public AdminUserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToDTO(user);
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

        // Load matron profile data if user is a MATRON
        if (user.getRole() == User.UserRole.MATRON) {
            matronProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                dto.setExperienceYears(profile.getYearsOfExperience());
                dto.setServiceArea(profile.getLocation());
                dto.setPriceRange(profile.getPricePerMonth() != null ? String.valueOf(profile.getPricePerMonth()) : null);
                dto.setBio(profile.getBio());
            });
        }

        return dto;
    }

    public List<AdminUserDTO> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<AdminUserDTO> getMothers() {
        return getUsersByRole(User.UserRole.MOTHER);
    }

    public List<AdminUserDTO> getMatrons() {
        return getUsersByRole(User.UserRole.MATRON);
    }

    public List<AdminUserDTO> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<AdminUserDTO> searchUsersByPhone(String phone) {
        return userRepository.findByPhoneContaining(phone).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Delete related profiles first to avoid foreign key constraint violations
        if (user.getRole() == User.UserRole.MOTHER) {
            motherProfileRepository.findByUserId(userId).ifPresent(motherProfileRepository::delete);
        } else if (user.getRole() == User.UserRole.MATRON) {
            matronProfileRepository.findByUserId(userId).ifPresent(matronProfileRepository::delete);
        }

        userRepository.deleteById(userId);
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public long getMothersCount() {
        return userRepository.countByRole(User.UserRole.MOTHER);
    }

    public long getMatronsCount() {
        return userRepository.countByRole(User.UserRole.MATRON);
    }

    public List<MatronProfile> getAllMatronProfiles() {
        return matronProfileRepository.findAll();
    }

    public MatronProfile getMatronProfileById(Long profileId) {
        return matronProfileRepository.findById(profileId)
            .orElseThrow(() -> new RuntimeException("Matron profile not found with id: " + profileId));
    }

    @Transactional
    public AdminUserDTO createUser(AdminCreateUserRequest request) {
        // Check if email already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Check if phone already exists
        if (request.getPhone() != null && userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone already exists: " + request.getPhone());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        // Admin-created users don't have passwords initially
        user.setPassword(null);
        user.setProfileCompleted(false);

        User savedUser = userRepository.save(user);

        // Create mother profile if user is a MOTHER
        if (request.getRole() == User.UserRole.MOTHER) {
            MotherProfile motherProfile = new MotherProfile();
            motherProfile.setUser(savedUser);
            motherProfile.setDueDate(request.getDueDate());
            motherProfile.setBabyBirthDate(request.getBabyBirthDate());
            motherProfile.setAddress(request.getAddress());
            motherProfile.setSpecialNeeds(request.getSpecialNeeds());
            motherProfileRepository.save(motherProfile);
        }

        return convertToDTO(savedUser);
    }

    @Transactional
    public AdminUserDTO updateUser(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if email is being changed and if it already exists
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Check if phone is being changed and if it already exists
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new RuntimeException("Phone already exists: " + request.getPhone());
            }
            user.setPhone(request.getPhone());
        }

        // Update fields if provided
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        User savedUser = userRepository.save(user);

        // Update mother profile if user is a MOTHER
        if (user.getRole() == User.UserRole.MOTHER) {
            MotherProfile motherProfile = motherProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    MotherProfile newProfile = new MotherProfile();
                    newProfile.setUser(user);
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
        }

        // Update matron profile if user is a MATRON
        if (user.getRole() == User.UserRole.MATRON) {
            MatronProfile matronProfile = matronProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    MatronProfile newProfile = new MatronProfile();
                    newProfile.setUser(user);
                    // Set default values for required fields
                    newProfile.setAge(0);
                    newProfile.setYearsOfExperience(0);
                    newProfile.setPricePerMonth(0.0);
                    newProfile.setLocation("");
                    return newProfile;
                });

            if (request.getAddress() != null) {
                matronProfile.setLocation(request.getAddress());
            }
            if (request.getBio() != null) {
                matronProfile.setBio(request.getBio());
            }
            if (request.getExperienceYears() != null) {
                matronProfile.setYearsOfExperience(request.getExperienceYears());
            }
            if (request.getPricePerMonth() != null) {
                matronProfile.setPricePerMonth(request.getPricePerMonth());
            }

            matronProfileRepository.save(matronProfile);
        }

        return convertToDTO(savedUser);
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
    public MatronProfile createMatron(CreateMatronRequest request) {
        // Check if email already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Check if phone already exists
        if (request.getPhone() != null && userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone already exists: " + request.getPhone());
        }

        // Create User with MATRON role
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.MATRON);
        user.setPassword(null); // Admin-created users don't have passwords initially
        user.setProfileCompleted(false);

        User savedUser = userRepository.save(user);

        // Create MatronProfile
        MatronProfile profile = new MatronProfile();
        profile.setUser(savedUser);
        profile.setAge(request.getAge());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setPricePerMonth(request.getMonthlyRate());
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());
        profile.setIsAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        return matronProfileRepository.save(profile);
    }
}
